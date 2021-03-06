package tfar.meandering;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DataSerializerEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfar.meandering.client.MeanderingClient;
import tfar.meandering.mixin.*;
import tfar.meandering.net.PacketHandler;
import tfar.meandering.net.S2COtherMeanderingPacket;
import tfar.meandering.world.MSavedData;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Meandering.MODID)
public class Meandering {
    // Directly reference a log4j logger.

    public static final String MODID = "meandering";

    private static final Logger LOGGER = LogManager.getLogger();

    public static final EntityType<IdlePlayer> IDLE_PLAYER = EntityType.Builder.create(IdlePlayer::new, EntityClassification.MISC).size(0.6F, 1.95F).build("idle_player");

    public static Meandering INSTANCE;

    public MSavedData data;

    public Meandering() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading

        INSTANCE = this;

        bus.addGenericListener(DataSerializerEntry.class, DataSerializerItemList::register);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        if (FMLEnvironment.dist.isClient())
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MeanderingClient::init);
        bus.addListener(this::attributes);
        bus.addGenericListener(EntityType.class, this::entities);
        MinecraftForge.EVENT_BUS.addListener(this::death);
        MinecraftForge.EVENT_BUS.addListener(this::spawn);
        MinecraftForge.EVENT_BUS.addListener(this::placeBlock);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
        MinecraftForge.EVENT_BUS.addListener(this::login);
        MinecraftForge.EVENT_BUS.addListener(this::attack);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(IDLE_PLAYER, IdlePlayer.registerAttributes().create());
    }

    private void entities(RegistryEvent.Register<EntityType<?>> e) {
        e.getRegistry().register(IDLE_PLAYER.setRegistryName("idle_player"));
    }

    private void serverStart(FMLServerStartingEvent e) {
        MinecraftServer server = e.getServer();
        ServerWorld level = server.getWorld(World.OVERWORLD);

        data = level.getSavedData().getOrCreate(() -> new MSavedData(MODID), MODID);
    }

    private void attack(LivingAttackEvent e) {
        Entity attacker = e.getSource().getTrueSource();
        if (attacker instanceof PlayerEntity && meanderingSideSafe((PlayerEntity)attacker)) {
            e.setCanceled(true);
        }
    }


    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.registerMessages();

        Predicate<Entity> canAiTarget = EntityPredicates.CAN_AI_TARGET;

        Predicate<Entity> newCanAiTarget = entity -> canAiTarget.test(entity) && !isMeandering(entity);

        EntityPredicatesAccess.setCAN_AI_TARGET(newCanAiTarget);

        Predicate<Entity> canHAiTarget = EntityPredicates.CAN_HOSTILE_AI_TARGET;

        Predicate<Entity> newCanHAiTarget = entity -> {
            boolean b = canHAiTarget.test(entity) && !isMeandering(entity);
            return b;
        };

        EntityPredicatesAccess.setCAN_HOSTILE_AI_TARGET(newCanHAiTarget);

    }

    public static void handle(ServerPlayerEntity player) {

        boolean active = INSTANCE.data.isActive(player);

        if (active) {
            INSTANCE.data.disable(player);
        } else {
            INSTANCE.data.enable(player);
        }
    }

    private void death(LivingDeathEvent e) {
        LivingEntity entity = e.getEntityLiving();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (meanderingSideSafe(player)) {
                INSTANCE.data.disable((ServerPlayerEntity) player);
            } else {
                UUID uuid = player.getGameProfile().getId();
                ServerWorld world = (ServerWorld) player.world;
                world.getEntities().filter(IdlePlayer.class::isInstance).map(IdlePlayer.class::cast)
                        .forEach(idlePlayer -> {
                            if (idlePlayer.getPlayerUUID().isPresent()) {
                                UUID uuid1 = idlePlayer.getPlayerUUID().get();
                                if (uuid1.equals(uuid)) {
                                    idlePlayer.remove();
                                }
                            }
                        });
            }
        }
    }

    private void login(PlayerEvent.PlayerLoggedInEvent e) {
        data.login((ServerPlayerEntity) e.getPlayer());
    }

    private void spawn(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof MobEntity && !entity.world.isRemote) {
            MobEntity mob = (MobEntity) entity;
            GoalSelector goalSelector = mob.goalSelector;
            GoalSelector targetSelector = mob.targetSelector;
            Set<PrioritizedGoal> goals = ((GoalSelectorAccess) goalSelector).getGoals();
            Set<PrioritizedGoal> targets = ((GoalSelectorAccess) targetSelector).getGoals();

            boolean addTarget = false;
            boolean addLook = false;

            NearestAttackableTargetGoal<?> sAttackableTargetGoal = null;
            LookAtGoal sLookAtGoal = null;

            for (PrioritizedGoal target : targets) {
                Goal unwrappedGoal = target.getGoal();
                if (unwrappedGoal instanceof NearestAttackableTargetGoal) {
                    NearestAttackableTargetGoal<?> attackableTargetGoal = (NearestAttackableTargetGoal<?>) unwrappedGoal;

                    Class<?> clasz = ((NearestAttackableTargetGoalAccess) attackableTargetGoal).getTargetClass();
                    if (clasz == PlayerEntity.class) {
                        sAttackableTargetGoal = attackableTargetGoal;
                        addTarget = true;
                        break;
                    }
                }
            }

            if (addTarget) {
                NearestAttackableTargetGoalAccess access = (NearestAttackableTargetGoalAccess)sAttackableTargetGoal;

                Predicate<LivingEntity> currentPredicate = ((EntityPredicateAccess)access.getTargetEntitySelector()).getCustomPredicate();

                Predicate<LivingEntity> newPredicate = null;

                if (currentPredicate != null && mob instanceof IAngerable) {
                    IAngerable angerable = (IAngerable)mob;

                    newPredicate = livingEntity -> {
                                PlayerEntity player = ((IdlePlayer) livingEntity).getServerPlayer();
                                return angerable.func_241357_a_(player.world) ||
                                        player.getUniqueID().equals(angerable.getAngerTarget());
                    };
                }

                NearestAttackableTargetGoal<IdlePlayer> newTarget = new NearestAttackableTargetGoal<>(mob, IdlePlayer.class,
                        access.getTargetChance(),
                        access.getShouldCheckSight(),
                        access.getNearbyOnly(),
                        newPredicate);
                mob.targetSelector.addGoal(2, newTarget);
            }

            for (PrioritizedGoal goal : goals) {
                Goal unwrappedGoal = goal.getGoal();
                if (unwrappedGoal instanceof LookAtGoal) {
                    LookAtGoal lookAtGoal = (LookAtGoal) unwrappedGoal;

                    Class<?> clasz = ((LookAtGoalAccess) lookAtGoal).getWatchedClass();
                    if (clasz == PlayerEntity.class) {
                        sLookAtGoal = lookAtGoal;
                        addLook = true;
                        break;
                    }
                }
            }

            if (addLook) {
                LookAtGoal newTarget = new LookAtGoal(mob, IdlePlayer.class,
                        (((LookAtGoalAccess) sLookAtGoal).getMaxDistance()),
                        ((LookAtGoalAccess)sLookAtGoal).getChance());
                mob.goalSelector.addGoal(6, newTarget);
            }
        }
    }

    private void placeBlock(BlockEvent.EntityPlaceEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (meanderingSideSafe(player)) {
                Vector3d center = centerSideSafe(player);
                if (center != null) {
                    BlockPos pos = e.getPos();
                    double sqDist = pos.distanceSq(center, true);
                    if (sqDist > 16 * 16) {
                        e.setCanceled(true);
                    }
                }
            }
        }
    }

    public static boolean isMeandering(Entity entity) {
        return entity instanceof PlayerEntity && meanderingSideSafe((PlayerEntity) entity);
    }

    public static boolean meanderingSideSafe(PlayerEntity player) {
        if (player.world.isRemote) {
            return MeanderingClient.isMeanderingClient(player);
        }
        return INSTANCE.data.isActive((ServerPlayerEntity) player);
    }

    private static Vector3d centerSideSafe(PlayerEntity player) {
        if (player.world.isRemote) {
            return MeanderingClient.center;
        }
        return INSTANCE.data.center((ServerPlayerEntity) player);
    }
}
