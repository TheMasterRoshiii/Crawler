package me.master.owleaf.crawler.entities;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "crawler");

    public static final RegistryObject<EntityType<CrawlerTrapEntity>> CRAWLER_TRAP =
            ENTITY_TYPES.register("crawler_trap", () -> EntityType.Builder.of(CrawlerTrapEntity::new, MobCategory.MISC)
                    .sized(2.0f, 3.0f)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("crawler_trap"));

}
