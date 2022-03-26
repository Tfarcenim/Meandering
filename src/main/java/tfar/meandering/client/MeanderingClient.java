package tfar.meandering.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import tfar.meandering.Meandering;
import tfar.meandering.net.C2SKeybindPacket;
import tfar.meandering.net.PacketHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MeanderingClient {

    public static final KeyBinding KEY = new KeyBinding("hotkey", GLFW.GLFW_KEY_Y, Meandering.MODID);

    public static boolean selfMeandering;
    public static Set<UUID> otherMeandering = new HashSet<>();

    public static Vector3d center;

    public static void init(FMLClientSetupEvent e) {
        ClientRegistry.registerKeyBinding(KEY);
        MinecraftForge.EVENT_BUS.addListener(MeanderingClient::keyPressed);
        MinecraftForge.EVENT_BUS.addListener(MeanderingClient::move);
        MinecraftForge.EVENT_BUS.addListener(MeanderingClient::click);
        MinecraftForge.EVENT_BUS.addListener(MeanderingClient::render);
        MinecraftForge.EVENT_BUS.addListener(MeanderingClient::logout);
        RenderingRegistry.registerEntityRenderingHandler(Meandering.IDLE_PLAYER,IdlePlayerRenderer::new);
    }

    private static void render(RenderPlayerEvent.Pre e) {
        if (Meandering.meanderingSideSafe(e.getPlayer()) && !(e.getPlayer() instanceof IdleClientPlayer)) {
            e.setCanceled(true);
        }
    }

    private static void keyPressed(InputEvent.KeyInputEvent e) {
        while (KEY.isPressed()) {
            PacketHandler.sendToServer(new C2SKeybindPacket());
            if (!selfMeandering) {
                center = Minecraft.getInstance().player.getPositionVec();
            }
        }
    }

    private static void click(InputEvent.ClickInputEvent e) {
        if (selfMeandering && e.isAttack()) {
            e.setCanceled(true);
        }
    }

    private static void move(InputUpdateEvent e) {
        Vector3d pos = Minecraft.getInstance().player.getPositionVec();

        if (selfMeandering && !travelRange(pos)) {

            PlayerEntity player = Minecraft.getInstance().player;

            Vector3d dif = pos.subtract(center);

            dif = dif.normalize();

            double m = .1;

            player.setVelocity(-dif.x * m,-dif.y * m,-dif.z * m);

        }
    }

    private static void logout(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        selfMeandering = false;
        otherMeandering.clear();
    }

    private static boolean travelRange(Vector3d pos) {
        return pos.squareDistanceTo(center) < 32 * 32;
    }

    public static void handle(boolean meander,UUID uuid,Vector3d center) {
        if (Minecraft.getInstance().player.getGameProfile().getId().equals(uuid)) {
            selfMeandering = meander;
            center = MeanderingClient.center;
        } else {
            if (meander) {
                otherMeandering.add(uuid);
            } else {
                otherMeandering.remove(uuid);
            }
        }
    }

    public static boolean isMeanderingClient(PlayerEntity player) {
        return (player == Minecraft.getInstance().player) ? selfMeandering : otherMeandering.contains(player.getGameProfile().getId());
    }
}
