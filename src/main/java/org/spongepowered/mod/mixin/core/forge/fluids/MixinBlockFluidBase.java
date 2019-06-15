/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.mixin.core.forge.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.mod.mixin.core.block.MixinBlock;

import java.util.Map;

@Mixin(value = BlockFluidBase.class)
public abstract class MixinBlockFluidBase extends MixinBlock implements IMixinBlock {

    @Shadow @Final public static PropertyInteger LEVEL;
    @Shadow protected int tickRate;

    @Redirect(method = "canDisplace",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
            remap = false
        )
    )
    private Object getDisplacementWithSponge(Map map, Object key, IBlockAccess world, BlockPos pos) {
        if (!(world instanceof IMixinWorld) || ((IMixinWorld) world).isFake()) {
            return map.get(key);
        }
        if (!((Boolean) map.get(key))) {
            return Boolean.FALSE;
        }
        ChangeBlockEvent.Pre event = SpongeCommonEventFactory.callChangeBlockEventPre((IMixinWorldServer) world, pos);
        if (event.isCancelled()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Inject(method = "canDisplace",
        cancellable = true,
        remap = false,
        locals = LocalCapture.CAPTURE_FAILSOFT,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/BlockFluidBase;getDensity(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I",
            remap = false
        )
    )
    private void onSpongeInjectFailEvent(IBlockAccess world, BlockPos pos, CallbackInfoReturnable<Boolean> cir, IBlockState state, Block block) {
        if (!(world instanceof IMixinWorld) || ((IMixinWorld) world).isFake()) {
            return;
        }
        ChangeBlockEvent.Pre event = SpongeCommonEventFactory.callChangeBlockEventPre((IMixinWorldServer) world, pos);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
    

}