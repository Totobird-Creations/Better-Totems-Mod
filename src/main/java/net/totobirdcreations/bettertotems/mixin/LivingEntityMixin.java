package net.totobirdcreations.bettertotems.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.totobirdcreations.bettertotems.Mod;
import net.totobirdcreations.bettertotems.util.mixin.ILivingEntityMixin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntityMixin {

	private static final String ID = "bettertotems";

	private final LivingEntity self = (LivingEntity)(Object) this;

	private boolean hasVoidLevitation  = false;
	private boolean hasVoidSlowFalling = false;

	@Override
	public boolean hasVoidLevitation() {
		return this.hasVoidLevitation;
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
	private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean(ID + ".hasVoidLevitation"  , this.hasVoidLevitation  );
		nbt.putBoolean(ID + ".hasVoidSlowFalling" , this.hasVoidSlowFalling );
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
	private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		this.hasVoidLevitation  = nbt.getBoolean(ID + ".hasVoidLevitation"  );
		this.hasVoidSlowFalling = nbt.getBoolean(ID + ".hasVoidSlowFalling" );
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		if (this.hasVoidLevitation) {
			if (! self.hasStatusEffect(StatusEffects.LEVITATION)) {
				this.hasVoidLevitation = false;
			} else if (self.getY() >= (double) self.world.getBottomY()) {
				self.removeStatusEffect(StatusEffects.LEVITATION);
				self.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, -1, 127, true, true, true));
				this.hasVoidSlowFalling = true;
			}
		}
		if (this.hasVoidSlowFalling) {
			if (! self.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
				this.hasVoidSlowFalling = false;
			} else if (self.isOnGround()) {
				self.removeStatusEffect(StatusEffects.SLOW_FALLING);
				this.hasVoidSlowFalling = false;
			}
		}
	}

	@Inject(method = "tickInVoid", at = @At("HEAD"), cancellable = true)
	private void tickInVoid(CallbackInfo ci) {
		if (this.hasVoidLevitation) {
			ci.cancel();
		}
	}

	@Redirect(method = "tryUseTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
	private boolean tryUseTotemBypass(DamageSource instance, TagKey<DamageType> tag) {
		return (! instance.isOf(DamageTypes.OUT_OF_WORLD)) && instance.isIn(tag);
	}

	@ModifyVariable(method = "tryUseTotem", at = @At(value = "LOAD", ordinal = 0), index = 2)
	private @Nullable ItemStack tryUseTotemInventory(ItemStack prev) {
		if (self.getWorld().getGameRules().getBoolean(Mod.TOTEMS_IN_INVENTORY) && prev == null && self instanceof ServerPlayerEntity player) {
			for (int i = 0; i < player.getInventory().size(); i++) {
				ItemStack stack = player.getInventory().getStack(i);
				if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
					ItemStack copy = stack.copy();
					stack.decrement(1);
					return copy;
				}
			}
		}
		return prev;
	}

	@Inject(method = "tryUseTotem", at = @At(value = "RETURN"))
	private void tryUseTotemLevitation(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (self.getWorld().getGameRules().getBoolean(Mod.TOTEMS_IN_VOID) && cir.getReturnValue() && source.isOf(DamageTypes.OUT_OF_WORLD)) {
			self.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, -1, 127, true, true, true));
			self.removeStatusEffect(StatusEffects.SLOW_FALLING);
			this.hasVoidLevitation  = true;
			this.hasVoidSlowFalling = false;
		}
	}

}