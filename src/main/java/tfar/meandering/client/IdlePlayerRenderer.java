package tfar.meandering.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import tfar.meandering.IdlePlayer;

import java.util.UUID;
import java.util.WeakHashMap;

public class IdlePlayerRenderer extends EntityRenderer<IdlePlayer> {

    private final PlayerRenderer playerRenderer;
    private final PlayerRenderer smallPlayerRenderer;
    private final WeakHashMap<IdlePlayer,IdleClientPlayer> players;

    protected IdlePlayerRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        players = new WeakHashMap<>();

        playerRenderer = new NamelessPlayerRenderer(renderManager);
        smallPlayerRenderer = new NamelessPlayerRenderer(renderManager,true);
    }

    @Override
    public ResourceLocation getEntityTexture(IdlePlayer entity) {
        return null;
    }

    @Override
    public void render(IdlePlayer entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
        matrixStack.push();

        //matrixStack.rotate(Vector3f.YP.rotationDegrees(-entity.getYRot()));

        //matrixStack.rotate(Vector3f.XP.rotationDegrees(-90F));
      //  matrixStack.translate(0D, -1D, 2.01D / 16D);

        AbstractClientPlayerEntity abstractClientPlayerEntity =
                players.compute(entity, (a,b) -> new IdleClientPlayer((ClientWorld) entity.world,
                        new GameProfile(entity.getPlayerUUID().orElse(new UUID(0L, 0L)), entity.getPlayerName()), entity.getHeldEquipment(),entity.getArmorInventoryList(), entity.getPlayerModelFlag()));
        if (isSlim(entity.getPlayerUUID().orElse(new UUID(0L, 0L)))) {
            smallPlayerRenderer.render(abstractClientPlayerEntity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
        } else {
            playerRenderer.render(abstractClientPlayerEntity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
        }
        matrixStack.pop();
    }

    public static boolean isSlim(UUID uuid) {
        NetworkPlayerInfo networkplayerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
        return networkplayerinfo == null ? (uuid.hashCode() & 1) == 1 : networkplayerinfo.getSkinType().equals("slim");
    }

    @Override
    protected void renderName(IdlePlayer entityIn, ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        //why does this die?
        //super.renderName(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn);
    }
}
