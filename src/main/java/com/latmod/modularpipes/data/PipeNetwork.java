package com.latmod.modularpipes.data;

import com.feed_the_beast.ftbl.lib.util.MapUtils;
import com.google.common.base.Preconditions;
import com.latmod.modularpipes.ModularPipes;
import com.latmod.modularpipes.net.MessageUpdateItems;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class PipeNetwork
{
    private static final TIntObjectHashMap<PipeNetwork> NETWORK_MAP = new TIntObjectHashMap<>();
    private static final ItemStack FIREWORKS_ITEM = new ItemStack(Items.FIREWORKS);
    private static final Predicate<Map.Entry<Integer, TransportedItem>> REMOVE_PREDICATE = entry -> entry.getValue().action.remove();

    public static PipeNetwork get(World world)
    {
        Preconditions.checkArgument(!world.isRemote, "PipeNetwork can only be called from server side!");

        int dim = world.provider.getDimension();
        PipeNetwork net = NETWORK_MAP.get(dim);

        if(net == null)
        {
            net = new PipeNetwork(world);
            NETWORK_MAP.put(dim, net);
        }

        return net;
    }

    public static void clearAll()
    {
        NETWORK_MAP.clear();
    }

    public static void test(World world, BlockPos pos)
    {
        if(!world.isRemote)
        {
            world.spawnEntity(new EntityFireworkRocket(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, FIREWORKS_ITEM));
        }
    }

    // End of static //

    public final World world;
    public boolean loaded;
    private final Map<BlockPos, Node> nodeMap = new HashMap<>();
    private final List<Link> linkList = new ArrayList<>();
    private final List<Link> linkListTemp = new ArrayList<>();
    public final Map<Integer, TransportedItem> items = new HashMap<>();
    private int nextItemId = 0;
    private final Map<Integer, TransportedItem> updateCache = new HashMap<>();
    private boolean itemsRemoved = false;

    private final Consumer<TransportedItem> foreachUpdate = item ->
    {
        item.update();

        if(item.action.remove())
        {
            itemsRemoved = true;
        }

        if(item.action.update())
        {
            updateCache.put(item.id, item);
        }
    };

    private PipeNetwork(World w)
    {
        world = w;
    }

    public void load()
    {
        clear();
        loaded = true;
        File dir = new File(((WorldServer) world).getChunkSaveLocation(), "data/modularpipes");
        // ModularPipes.LOGGER.info("Loading pipe info from " + dir.getAbsolutePath());

        File file = new File(dir, "modularpipes.dat");

        if(!file.exists())
        {
            return;
        }

        NBTTagCompound nbt;

        try
        {
            nbt = CompressedStreamTools.read(file);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return;
        }

        NBTTagList list = nbt.getTagList("Nodes", Constants.NBT.TAG_COMPOUND);

        for(int i = 0; i < list.tagCount(); i++)
        {
            int[] pos = list.getIntArrayAt(i);

            if(pos.length >= 3)
            {
                Node node = new Node(this, new BlockPos(pos[0], pos[1], pos[2]));
                nodeMap.put(node.pos, node);
            }
        }

        list = nbt.getTagList("PathList", Constants.NBT.TAG_COMPOUND);

        for(int i = 0; i < list.tagCount(); i++)
        {
            linkList.add(new Link(this, list.getCompoundTagAt(i)));
        }

        list = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        for(int i = 0; i < list.tagCount(); i++)
        {
            TransportedItem item = new TransportedItem();
            NBTTagCompound nbt1 = list.getCompoundTagAt(i);
            item.action = TransportedItem.Action.NONE;
            item.stack = new ItemStack(nbt1.getCompoundTag("Item"));
            item.dimension = nbt1.getInteger("Dim");

            item.path.clear();
            NBTTagList pathTag = nbt1.getTagList("Link", Constants.NBT.TAG_INT_ARRAY);
            for(int j = 0; j < pathTag.tagCount(); j++)
            {
                int pos[] = pathTag.getIntArrayAt(j);

                if(pos.length >= 3)
                {
                    item.path.add(new BlockPos(pos[0], pos[1], pos[2]));
                }
            }

            item.filters = nbt1.getInteger("Filters");
            item.speed = nbt1.getFloat("Speed");
            item.progress = nbt1.getFloat("Progress");

            if(item.stack.isEmpty())
            {
                item.action = TransportedItem.Action.REMOVE;
            }

            if(item.action != TransportedItem.Action.REMOVE)
            {
                item.action = TransportedItem.Action.UPDATE;
                items.put(item.id, item);
            }
        }
    }

    public void unload()
    {
        loaded = false;

        for(Node node : getNodes())
        {
            node.clearCache();
        }

        NETWORK_MAP.remove(world.provider.getDimension());
    }

    public void clear()
    {
        nodeMap.clear();
        linkList.clear();
        items.clear();
    }

    public void save()
    {
        try
        {
            File dir = new File(((WorldServer) world).getChunkSaveLocation(), "data/modularpipes");

            File file = new File(dir, "modularpipes.dat");

            if(!file.exists() && nodeMap.isEmpty() && linkList.isEmpty() && items.isEmpty())
            {
                return;
            }

            if(!dir.exists())
            {
                dir.mkdirs();
            }

            //ModularPipes.LOGGER.info("Saved pipe info to " + dir.getAbsolutePath());
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagCompound nbt1;
            NBTTagList list = new NBTTagList();

            for(Node node : getNodes())
            {
                //nbt1 = new NBTTagCompound();
                //nbt1.setIntArray("Pos", new int[] {node.getPos().getX(), node.getPos().getY(), node.getPos().getZ()});
                //list.appendTag(nbt1);
                list.appendTag(new NBTTagIntArray(new int[] {node.pos.getX(), node.pos.getY(), node.pos.getZ()}));
            }

            nbt.setTag("Nodes", list);
            list = new NBTTagList();

            for(Link link : linkList)
            {
                //link.simplify();

                nbt1 = new NBTTagCompound();
                NBTTagList list1 = new NBTTagList();

                for(BlockPos pos : link.path)
                {
                    list1.appendTag(new NBTTagIntArray(new int[] {pos.getX(), pos.getY(), pos.getZ()}));
                }

                nbt1.setTag("Link", list1);
                nbt1.setFloat("Length", link.length);
                nbt1.setFloat("ActualLength", link.actualLength);

                list1.appendTag(nbt1);
            }
            nbt.setTag("PathList", list);
            list = new NBTTagList();

            for(TransportedItem item : items.values())
            {
                nbt1 = new NBTTagCompound();
                nbt1.setTag("Item", item.stack.serializeNBT());
                nbt1.setInteger("Dim", item.dimension);

                NBTTagList pathTag = new NBTTagList();

                for(BlockPos p : item.path)
                {
                    pathTag.appendTag(new NBTTagIntArray(new int[] {p.getX(), p.getY(), p.getZ()}));
                }

                nbt1.setTag("Link", pathTag);
                nbt1.setInteger("Filters", item.filters);
                nbt1.setFloat("Speed", item.speed);
                nbt1.setFloat("Progress", item.progress);
                list.appendTag(nbt1);
            }

            nbt.setTag("Items", list);
            CompressedStreamTools.write(nbt, file);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Nullable
    public Node getNode(BlockPos pos)
    {
        return nodeMap.get(pos);
    }

    public void setNode(BlockPos pos, @Nullable Node node)
    {
        if(node == null)
        {
            nodeMap.remove(pos);
            //ModularPipes.LOGGER.info("Node @ " + pos + " removed");
        }
        else
        {
            nodeMap.put(pos, node);
            //ModularPipes.LOGGER.info("Node @ " + pos + " placed");
        }
    }

    public Collection<Node> getNodes()
    {
        return nodeMap.values();
    }

    public List<Link> getPathList(BlockPos pos, boolean useTempList)
    {
        linkListTemp.clear();

        for(Link path : linkList)
        {
            if(path.contains(pos))
            {
                linkListTemp.add(path);
            }
        }

        return linkListTemp;
    }

    @Nullable
    public Link getBestPath(BlockPos from, BlockPos to)
    {
        List<Link> list = getPathList(from, true);

        if(list.isEmpty())
        {
            return null;
        }

        Iterator<Link> iterator = list.iterator();

        while(iterator.hasNext())
        {
            if(!iterator.next().contains(to))
            {
                iterator.remove();
            }
        }

        if(list.isEmpty())
        {
            return null;
        }
        else if(list.size() > 1)
        {
            list.sort(Link.COMPARATOR);
        }

        return list.get(0);
    }

    public void addOrUpdatePipe(BlockPos pos, IBlockState state)
    {
        if(!loaded || !(state.getBlock() instanceof IPipeBlock))
        {
            return;
        }

        removeLinkAt(pos, state);

        if(((IPipeBlock) state.getBlock()).isNode(world, pos, state))
        {
            Node node = new Node(this, pos);

            for(EnumFacing facing : EnumFacing.VALUES)
            {
                IBlockState state1 = world.getBlockState(pos.offset(facing));

                if(state1.getBlock() instanceof IPipeBlock)
                {
                    Link link = findNode(node, facing);

                    if(link != null)
                    {
                        //ModularPipes.LOGGER.info("Found link " + link);
                        linkList.add(link);
                    }
                }
            }
        }
        else
        {
            //ModularPipes.LOGGER.info("Placed pipe @ " + pos);
        }
    }

    @Nullable
    private Link findNode(Node start, EnumFacing facing)
    {
        List<BlockPos> list = new ArrayList<>();
        HashSet<BlockPos> set = new HashSet<>();
        float length = 0F;
        list.add(start.pos);
        set.add(start.pos);
        int actualLength = 0;
        BlockPos pos = start.pos.offset(facing);
        EnumFacing source = facing.getOpposite();
        IBlockState state1;

        while(actualLength < 256) //TODO: Config option
        {
            state1 = world.getBlockState(pos);

            if(!(state1.getBlock() instanceof IPipeBlock))
            {
                //ModularPipes.LOGGER.warn("Block not a pipe @ " + pos);
                return null;
            }

            IPipeBlock pipe = (IPipeBlock) state1.getBlock();

            if(pipe.isNode(world, pos, state1))
            {
                list.add(pos);
                Link link = new Link(this, UUID.randomUUID());
                link.path = list;
                link.actualLength = actualLength;
                link.length = length;
                //path.simplify();
                return link;
            }
            else
            {
                EnumFacing facing1 = pipe.getPipeFacing(world, pos, state1, source);

                if(facing1 == source)
                {
                    //ModularPipes.LOGGER.warn("Dead end @ " + pos);
                    return null;
                }
                else
                {
                    length += 1F / pipe.getSpeedModifier(world, pos, state1);
                    source = facing1.getOpposite();
                    pos = pos.offset(facing1);

                    if(set.contains(pos))
                    {
                        //ModularPipes.LOGGER.warn("Loop @ " + pos);
                        return null;
                    }

                    set.add(pos);
                    list.add(pos);
                }
            }

            actualLength++;
        }

        //ModularPipes.LOGGER.warn("Path too long!");
        return null;
    }

    public void removeLinkAt(BlockPos pos, IBlockState state)
    {
        if(!loaded)
        {
            return;
        }

        Iterator<Link> iterator = linkList.iterator();

        while(iterator.hasNext())
        {
            Link p = iterator.next();

            if(p.contains(pos))
            {
                //ModularPipes.LOGGER.info("Link " + p.getId() + " removed from " + pos);
                //p.onRemoved(worldIn);
                iterator.remove();
            }
        }
    }

    public void addItem(TransportedItem item)
    {
        item.id = ++nextItemId;
        item.action = TransportedItem.Action.UPDATE;

        if(nextItemId == 2000000000)
        {
            nextItemId = 0;
        }

        items.put(item.id, item);
    }

    public boolean generatePath(ModuleContainer container, TransportedItem item)
    {
        for(Link link : getPathList(container.getTile().getPos(), true))
        {
            for(BlockPos pos : link.path)
            {
                PipeNetwork.test(container.getTile().getWorld(), pos);
            }
        }

        return false;
    }

    public void update()
    {
        itemsRemoved = false;
        updateCache.clear();
        items.values().forEach(foreachUpdate);

        if(itemsRemoved)
        {
            MapUtils.removeAll(items, REMOVE_PREDICATE);
        }

        if(!updateCache.isEmpty())
        {
            sync(false);
        }
    }

    public void sync(boolean all)
    {
        ModularPipes.LOGGER.info("Synced items: " + (all ? items : updateCache));
        new MessageUpdateItems(all ? items : updateCache).sendToDimension(world.provider.getDimension());
    }

    public void playerLoggedIn(EntityPlayer playerMP)
    {
        new MessageUpdateItems(items).sendTo(playerMP);
    }
}