package com.example.totemimpact;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class TotemImpactClient implements ClientModInitializer {
    public static final String MOD_ID = "totemimpact";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(DeathFx::tick);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "impact_scene"),
                ImpactScene::render
        );
    }
}
