package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_EM_research;
import com.impact.mods.GregTech.tileentities.multi.debug.GTMTE_MBBase;
import com.impact.mods.GregTech.tileentities.multi.debug.GT_MetaTileEntity_MultiParallelBlockBase;
import com.impact.mods.GregTech.tileentities.storage.GTMTE_LapPowerStation;
import gregtech.api.enums.GT_Values;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicBatteryBuffer;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicMachine;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Transformer;
import gregtech.api.util.GT_Utility;
import gregtech.common.covers.GT_Cover_Fluidfilter;
import gregtech.common.tileentities.boilers.GT_MetaTileEntity_Boiler_Solar;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_PrimitiveBlastFurnace;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tterrag.wailaplugins.api.Plugin;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import static mcp.mobius.waila.api.SpecialChars.*;

@Plugin(name = "Gregtech5U", deps = "gregtech")
public class PluginGregtech5U extends PluginBase
{

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        addConfig("machineFacing");
        addConfig("transformer");
        addConfig("solar");
        addConfig("multiblock");
        addConfig("fluidFilter");
        addConfig("basicmachine");
        registerBody(BaseTileEntity.class);
        registerNBT(BaseTileEntity.class);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unused")
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        final TileEntity tile = accessor.getTileEntity();
        MovingObjectPosition pos = accessor.getPosition();
        NBTTagCompound tag = accessor.getNBTData();
        final int side = (byte)accessor.getSide().ordinal();

        final IGregTechTileEntity tBaseMetaTile = tile instanceof IGregTechTileEntity ? ((IGregTechTileEntity) tile) : null;
        final IMetaTileEntity tMeta = tBaseMetaTile != null ? tBaseMetaTile.getMetaTileEntity() : null;
        final BaseMetaTileEntity mBaseMetaTileEntity = tile instanceof  BaseMetaTileEntity ? ((BaseMetaTileEntity) tile) : null;
        final GT_MetaTileEntity_MultiBlockBase multiBlockBase = tMeta instanceof GT_MetaTileEntity_MultiBlockBase ? ((GT_MetaTileEntity_MultiBlockBase) tMeta) : null;
        final GT_MetaTileEntity_BasicMachine BasicMachine = tMeta instanceof GT_MetaTileEntity_BasicMachine ? ((GT_MetaTileEntity_BasicMachine) tMeta) : null;
        final GT_MetaTileEntity_MultiParallelBlockBase MultiParallel = tMeta instanceof GT_MetaTileEntity_MultiParallelBlockBase ? ((GT_MetaTileEntity_MultiParallelBlockBase) tMeta) : null;
        final GTMTE_MBBase multiBlockBaseImpact = tMeta instanceof GTMTE_MBBase ? ((GTMTE_MBBase) tMeta) : null;
        final GT_MetaTileEntity_BasicBatteryBuffer bateryBuffer = tMeta instanceof GT_MetaTileEntity_BasicBatteryBuffer ? ((GT_MetaTileEntity_BasicBatteryBuffer) tMeta) : null;
        final GTMTE_LapPowerStation LapBuffer = tMeta instanceof GTMTE_LapPowerStation ? ((GTMTE_LapPowerStation) tMeta) : null;
        final GT_MetaTileEntity_EM_research Research = tMeta instanceof GT_MetaTileEntity_EM_research ? ((GT_MetaTileEntity_EM_research) tMeta) : null;


        final boolean showTransformer = tMeta instanceof GT_MetaTileEntity_Transformer && getConfig("transformer");
        final boolean showSolar = tMeta instanceof GT_MetaTileEntity_Boiler_Solar && getConfig("solar");
        final boolean allowedToWork = tag.hasKey("isAllowedToWork") && tag.getBoolean("isAllowedToWork");

        if (tBaseMetaTile != null && getConfig("fluidFilter")) {
            final String filterKey = "filterInfo" + side;
            if (tag.hasKey(filterKey)) {
                currenttip.add(tag.getString(filterKey));
            }
        }

        if (tMeta != null) {
            String facingStr = "Facing";
            if (showTransformer && tag.hasKey("isAllowedToWork")) {
                currenttip.add(
                    String.format(
                        "%s %d(%dA) -> %d(%dA)",
                        (allowedToWork ? (GREEN + "Step Down") : (RED + "Step Up")) + RESET,
                        tag.getLong("maxEUInput"),
                        tag.getLong("maxAmperesIn"),
                        tag.getLong("maxEUOutput"),
                        tag.getLong("maxAmperesOut")
                    )
                );
                facingStr = tag.getBoolean("isAllowedToWork") ? "Input" : "Output";
            }
            if (showSolar && tag.hasKey("calcificationOutput")) {
                currenttip.add(String.format((GOLD + "Solar Boiler Output: " + RESET + "%d/%d L/s"), tag.getInteger("calcificationOutput"), tag.getInteger("maxCalcificationOutput")));
            }

            if (tMeta instanceof  GT_MetaTileEntity_PrimitiveBlastFurnace){
                if(tag.getBoolean("incompleteStructurePrimitiveBlastFurnace")) {
                    currenttip.add(RED + "Incomplete Structure" + RESET);
                }

                currenttip.add(
                        String.format(
                                "Progress: %d s / %d s",
                                tag.getInteger("progressPrimitiveBlastFurnace"),
                                tag.getInteger("maxProgressPrimitiveBlastFurnace")
                        )
                );
            }

            if (mBaseMetaTileEntity != null && getConfig("machineFacing")) {
                final int facing = mBaseMetaTileEntity.getFrontFacing();
                if(showTransformer) {
                    if((side == facing && allowedToWork) || (side != facing && !allowedToWork)) {
                        currenttip.add(String.format(GOLD + "Input:" + RESET + " %d(%dA)", tag.getLong("maxEUInput"), tag.getLong("maxAmperesIn")));
                    } else {
                        currenttip.add(String.format(BLUE + "Output:" + RESET + " %d(%dA)", tag.getLong("maxEUOutput"), tag.getLong("maxAmperesOut")));
                    }
                } else {
                    currenttip.add(String.format("%s: %s", facingStr, ForgeDirection.getOrientation(facing).name()));
                }
            }


            if(multiBlockBase != null && getConfig("multiblock")) {
                if(tag.getBoolean("incompleteStructure")) {
                    currenttip.add(RED + "Incomplete Structure" + RESET);
                }
                currenttip.add((tag.getBoolean("hasProblems") ? (RED + "Need Maintenance") : GREEN + "Running Fine") + RESET + "  Efficiency: " + tag.getFloat("efficiency") + "%");

                currenttip.add(String.format("Progress: %d s / %d s", tag.getInteger("progress"), tag.getInteger("maxProgress")));

                if (MultiParallel != null && tag.getInteger("Parallel") > 1) currenttip.add(String.format("Parallel Point: %d", tag.getInteger("Parallel")));

                if(LapBuffer != null) {
                    currenttip.add("Stored: " + GREEN + NumberFormat.getNumberInstance().format(new BigInteger(tag.getByteArray("Stored"))) + RESET + " EU");
                    currenttip.add("Capacity: " + YELLOW + NumberFormat.getNumberInstance().format(new BigInteger(tag.getByteArray("Capacity"))) + RESET + " EU");
                    currenttip.add("Input: " + GREEN + NumberFormat.getNumberInstance().format(new BigInteger(tag.getByteArray("Input"))) + RESET + " EU/t");
                    currenttip.add("Output: " + RED + NumberFormat.getNumberInstance().format(new BigInteger(tag.getByteArray("Output"))) + RESET + " EU/t");
                }
                if(Research != null) {
                    currenttip.add("Computation Remaining: " + GREEN + NumberFormat.getNumberInstance().format(tag.getLong("computationRemaining")) + " / " + YELLOW + NumberFormat.getNumberInstance().format(tag.getInteger("computationRequired")));
                }
            }

            if(multiBlockBaseImpact != null && getConfig("multiblock")) {
                if(tag.getBoolean("incompleteStructureImpact")) {
                    currenttip.add(RED + "Incomplete Structure" + RESET);
                }
                currenttip.add((tag.getBoolean("hasProblemsImpact") ? (RED + "Need Maintenance") : GREEN + "Running Fine") + RESET + "  Efficiency: " + tag.getFloat("efficiencyImpact") + "%");

                currenttip.add(String.format("Progress: %d s / %d s", tag.getInteger("progressImpact"), tag.getInteger("maxProgressImpact")));
            }

            if (BasicMachine != null && getConfig("basicmachine")) {
                currenttip.add(String.format("Progress: %d s / %d s", tag.getInteger("progressSingleBlock"), tag.getInteger("maxProgressSingleBlock")));
                currenttip.add("Consumption: " + RED + tag.getInteger("EUOut") + RESET + " EU/t");
            }

            if(bateryBuffer != null && getConfig("basicmachine")) {
                currenttip.add("Used Capacity: " + GREEN + GT_Utility.formatNumbers(tag.getLong("nowStorage")) + RESET + " EU");
                currenttip.add("Total Capacity: " + YELLOW + GT_Utility.formatNumbers(tag.getLong("maxStorage")) + RESET + " EU");
                currenttip.add("In: " + GREEN + GT_Utility.formatNumbers(tag.getLong("energyInput")) + RESET + " EU/t");
                currenttip.add("Out: " + RED + GT_Utility.formatNumbers(tag.getLong("energyOutput")) + RESET + " EU/t");
            }

        }

    }


    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos)
    {
        final IGregTechTileEntity tBaseMetaTile = tile instanceof IGregTechTileEntity ? ((IGregTechTileEntity) tile) : null;
        final IMetaTileEntity tMeta = tBaseMetaTile != null ? tBaseMetaTile.getMetaTileEntity() : null;
        final GT_MetaTileEntity_MultiBlockBase multiBlockBase = tMeta instanceof GT_MetaTileEntity_MultiBlockBase ? ((GT_MetaTileEntity_MultiBlockBase) tMeta) : null;
        final GT_MetaTileEntity_BasicMachine BasicMachine = tMeta instanceof GT_MetaTileEntity_BasicMachine ? ((GT_MetaTileEntity_BasicMachine) tMeta) : null;
        final GT_MetaTileEntity_MultiParallelBlockBase MultiParallel = tMeta instanceof GT_MetaTileEntity_MultiParallelBlockBase ? ((GT_MetaTileEntity_MultiParallelBlockBase) tMeta) : null;
        final GTMTE_MBBase multiBlockBaseImpact = tMeta instanceof GTMTE_MBBase ? ((GTMTE_MBBase) tMeta) : null;
        final GT_MetaTileEntity_BasicBatteryBuffer bateryBuffer = tMeta instanceof GT_MetaTileEntity_BasicBatteryBuffer ? ((GT_MetaTileEntity_BasicBatteryBuffer) tMeta) : null;


        if (tMeta != null) {
            if (tMeta instanceof GT_MetaTileEntity_Transformer) {
                final GT_MetaTileEntity_Transformer transformer = (GT_MetaTileEntity_Transformer)tMeta;
                tag.setBoolean("isAllowedToWork", tMeta.getBaseMetaTileEntity().isAllowedToWork());
                tag.setLong("maxEUInput", transformer.maxEUInput());
                tag.setLong("maxAmperesIn", transformer.maxAmperesIn());
                tag.setLong("maxEUOutput", transformer.maxEUOutput());
                tag.setLong("maxAmperesOut", transformer.maxAmperesOut());
            } else if (tMeta instanceof GT_MetaTileEntity_Boiler_Solar) {
                final GT_MetaTileEntity_Boiler_Solar solar = (GT_MetaTileEntity_Boiler_Solar)tMeta;
                tag.setInteger("calcificationOutput", (solar.getCalcificationOutput()*20/25));
                tag.setInteger("maxCalcificationOutput", (solar.getBasicOutput()*20/25));
            } else if (tMeta instanceof  GT_MetaTileEntity_PrimitiveBlastFurnace) {
                final GT_MetaTileEntity_PrimitiveBlastFurnace blastFurnace = (GT_MetaTileEntity_PrimitiveBlastFurnace) tMeta;
                final int progress = blastFurnace.mProgresstime/20;
                final int maxProgress = blastFurnace.mMaxProgresstime/20;
                tag.setInteger("progressPrimitiveBlastFurnace", progress);
                tag.setInteger("maxProgressPrimitiveBlastFurnace", maxProgress);
                tag.setBoolean("incompleteStructurePrimitiveBlastFurnace", !blastFurnace.mMachine);
            }


            if (multiBlockBase != null) {
                final int problems = multiBlockBase.getIdealStatus() - multiBlockBase.getRepairStatus();
                final float efficiency = multiBlockBase.mEfficiency / 100.0F;
                final int progress = multiBlockBase.mProgresstime/20;
                final int maxProgress = multiBlockBase.mMaxProgresstime/20;

                tag.setBoolean("hasProblems", problems > 0);
                tag.setFloat("efficiency", efficiency);
                tag.setInteger("progress", progress);
                tag.setInteger("maxProgress", maxProgress);
                tag.setBoolean("incompleteStructure", (tBaseMetaTile.getErrorDisplayID() & 64) != 0);

                if (MultiParallel != null) {
                    final int Parallel = MultiParallel.mParallel;
                    tag.setInteger("Parallel", Parallel);
                }

                if(tMeta instanceof GTMTE_LapPowerStation) {
                    GTMTE_LapPowerStation mte = (GTMTE_LapPowerStation)tMeta;
                    final BigInteger Capacity = mte.capacity;
                    final BigInteger Stored = mte.stored;
                    final BigInteger Input = mte.intputLastTick;
                    final BigInteger Output = mte.outputLastTick;

                    tag.setByteArray("Capacity", Capacity.toByteArray());
                    tag.setByteArray("Stored", Stored.toByteArray());
                    tag.setByteArray("Input", Input.toByteArray());
                    tag.setByteArray("Output", Output.toByteArray());
                }
                if (tMeta instanceof GT_MetaTileEntity_EM_research) {
                    GT_MetaTileEntity_EM_research mte = (GT_MetaTileEntity_EM_research)tMeta;
                    final long computationRemaining = mte.computationRemaining/20L;
                    final long computationRequired = mte.computationRequired/20L;

                    tag.setLong("computationRemaining", computationRemaining);
                    tag.setLong("computationRequired", computationRequired);
                }
            }

            if (multiBlockBaseImpact != null) {
                final int problems = multiBlockBaseImpact.getIdealStatus() - multiBlockBaseImpact.getRepairStatus();
                final float efficiency = multiBlockBaseImpact.mEfficiency / 100.0F;
                final int progress = multiBlockBaseImpact.mProgresstime/20;
                final int maxProgress = multiBlockBaseImpact.mMaxProgresstime/20;

                tag.setBoolean("hasProblemsImpact", problems > 0);
                tag.setFloat("efficiencyImpact", efficiency);
                tag.setInteger("progressImpact", progress);
                tag.setInteger("maxProgressImpact", maxProgress);
                tag.setBoolean("incompleteStructureImpact", (tBaseMetaTile.getErrorDisplayID() & 64) != 0);
            }

            if (BasicMachine != null) {
                final int progressSingleBlock = BasicMachine.mProgresstime/20;
                final int maxProgressSingleBlock = BasicMachine.mMaxProgresstime/20;
                final int EUOut = BasicMachine.mEUt;
                tag.setInteger("progressSingleBlock", progressSingleBlock);
                tag.setInteger("maxProgressSingleBlock", maxProgressSingleBlock);
                tag.setInteger("EUOut", EUOut);
            }

            if (bateryBuffer != null) {
                long[] tmp = bateryBuffer.getStoredEnergy();
                long nowStorage = tmp[0];
                long maxStorage = tmp[1];

                long energyInput = bateryBuffer.getBaseMetaTileEntity().getAverageElectricInput();
                long energyOutput = bateryBuffer.getBaseMetaTileEntity().getAverageElectricOutput();
                tag.setLong("nowStorage", nowStorage);
                tag.setLong("maxStorage", maxStorage);
                tag.setLong("energyInput", energyInput);
                tag.setLong("energyOutput", energyOutput);
            }

        }
        if (tBaseMetaTile != null) {
            if (tBaseMetaTile instanceof BaseMetaPipeEntity) {
                for(byte side=0 ; side < 6 ; side++) {
                    if(tBaseMetaTile.getCoverBehaviorAtSide(side) instanceof GT_Cover_Fluidfilter) {
                        tag.setString("filterInfo" + side, tBaseMetaTile.getCoverBehaviorAtSide(side).getDescription(side, tBaseMetaTile.getCoverIDAtSide(side), tBaseMetaTile.getCoverDataAtSide(side), tBaseMetaTile));
                    }
                }
            }
        }

        tile.writeToNBT(tag);
    }
}
