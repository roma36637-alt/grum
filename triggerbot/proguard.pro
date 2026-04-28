# ProGuard rules for stealth trigger bot.
# Keep only the public surface that Fabric / Mixin looks up by name.
# Everything else gets renamed / inlined / obfuscated aggressively.

-dontwarn **
-dontnote **
-verbose

# Preserve annotations (Mixin framework reads them at runtime) and strip
# source file / line number info so stack traces leak nothing.
-keepattributes *Annotation*
-renamesourcefileattribute ""

# Aggressive optimisation / obfuscation.
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''
-allowaccessmodification
-mergeinterfacesaggressively
-optimizations !code/allocation/variable

# Fabric entrypoints — referenced by class name in fabric.mod.json.
-keep class com.stb.StbMod { *; }
-keep class com.stb.client.StbClient { *; }

# Mixin classes must keep their names (referenced by mixins.json) and
# @Inject/@At-annotated methods must retain their signatures.
-keep class com.stb.client.mixin.** { *; }
-keepclassmembers class com.stb.client.mixin.** {
    @org.spongepowered.asm.mixin.injection.Inject *;
    @org.spongepowered.asm.mixin.injection.Redirect *;
    @org.spongepowered.asm.mixin.injection.ModifyArg *;
    @org.spongepowered.asm.mixin.injection.ModifyArgs *;
    @org.spongepowered.asm.mixin.injection.ModifyVariable *;
    @org.spongepowered.asm.mixin.injection.ModifyConstant *;
    @org.spongepowered.asm.mixin.Overwrite *;
    @org.spongepowered.asm.mixin.Shadow *;
}

# Gson uses reflection on config fields, so keep them.
-keepclassmembers class com.stb.client.StbConfig {
    !transient <fields>;
    public static com.stb.client.StbConfig get();
    public void save();
}

# Don't touch anything from non-mod packages (Minecraft / Fabric / Mixin
# classes never end up inside the jar after loom's remap, but be safe).
-keep class net.minecraft.** { *; }
-keep class net.fabricmc.** { *; }
-keep class org.spongepowered.** { *; }
-keep class com.mojang.** { *; }

# Keep all runtime annotations — Mixin, Inject, etc. are looked up reflectively.

# Use unusual characters (Chinese-ideographs & zero-widths would confuse
# decompilers but break the JVM class format); stick with all-lowercase
# three-letter obfuscated names — ProGuard's default dictionary is fine.
