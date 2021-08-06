package com.paleimitations.schoolsofmagic.common.data.books;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.paleimitations.schoolsofmagic.common.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ParagraphPageElement extends PageElement {

	public final TranslationTextComponent textLocation;
	public final float scale;
    public final int fontColor;
	public final List<ParagraphBox> boxes;
    public List<String> text = Lists.newArrayList();
	public boolean tryLoad = true;

	public ParagraphPageElement(TranslationTextComponent textLocation, float scale, int fontColor, int target, ParagraphBox... boxes) {
		super(0,0, target);
		this.textLocation = textLocation;
		this.scale = scale;
        this.fontColor = fontColor;
		this.boxes = Lists.newArrayList(boxes);
	}

    public float fontScale() {
        return scale<=0? Config.Client.BOOK_FONT_SCALE.get().floatValue() : scale;
    }

    @Override
    public boolean isTarget(int i) {
        return i <= subpage;
    }

    @OnlyIn(Dist.CLIENT)
    public void loadText() {
	    ITextProperties properties = textLocation;
	    FontRenderer font = Minecraft.getInstance().font;
	    properties.visit((style, s) -> {
            if (!s.isEmpty()) {
                String[] strings = s.split("<~>");
                this.text = Lists.newArrayList(strings);
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasSubpage(int subpage) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.font;
        int boxId = 0;
        int linenumber = 0;
        for(String s : text) {
            boolean flag = true;
            while(flag){
                String remainder = null;
                if(boxes.size() > boxId && boxes.get(boxId)!=null) {
                    ParagraphBox box = boxes.get(boxId);
                    for (String s1 : listFormattedStringToWidth(s, Math.round(box.width / scale))) {
                        if((linenumber+1) * font.lineHeight <= Math.round(box.height / scale)) {
                            if(subpage == box.target)
                                return true;
                        }
                        else{
                            if(remainder==null)
                                remainder = s1;
                            else
                                remainder += s1;
                        }
                        ++linenumber;
                    }
                }
                else
                    flag = false;
                if(remainder==null)
                    flag = false;
                else {
                    boxId ++;
                    linenumber = 0;
                    s = remainder;
                }
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawElement(MatrixStack matrixStack, float mouseX, float mouseY, int xIn, int yIn, float zLevel, boolean isGUI, int subpage, int light) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.font;

        if(tryLoad && this.text.isEmpty()) {
            this.tryLoad = false;
            this.loadText();
        }

        matrixStack.pushPose();
        matrixStack.scale(scale, scale, scale);

        int boxId = 0;
        int linenumber = 0;
        for(String s : text) {
            boolean flag = true;
            while(flag) {
                String remainder = null;
                if(boxes.size() > boxId && boxes.get(boxId)!=null) {
                    ParagraphBox box = boxes.get(boxId);
                    int xI = Math.round((box.x + xIn) / scale);
                    int yI = Math.round((box.y + yIn) / scale);
                    for (String s1 : listFormattedStringToWidth(s, Math.round(box.width / scale))) {
                        if((linenumber+1) * font.lineHeight <= Math.round(box.height / scale)) {
                            if(subpage == box.target)
                                font.draw(matrixStack, s1, xI, yI + Math.round(linenumber * font.lineHeight), 0);
                        }
                        else {
                            if(remainder==null)
                                remainder = s1;
                            else
                                remainder += s1;
                        }
                        ++linenumber;
                    }
                }
                else
                    flag = false;
                if(remainder==null)
                    flag = false;
                else {
                    boxId ++;
                    linenumber = 0;
                    s = remainder;
                }
            }
        }

        matrixStack.popPose();
    }

    public static List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    private static String wrapFormattedStringToWidth(String str, int wrapWidth) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.font;
        int i = sizeStringToWidth(str, wrapWidth);

        if (str.length() <= i)
        {
            return str;
        }
        else
        {
            String s = str.substring(0, i);
            String s1 = /*font.getFormatFromString(s) +*/ str.substring(i);
            return s + "\n" + wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    private static int sizeStringToWidth(String str, int wrapWidth) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.font;
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k)
        {
            char c0 = str.charAt(k);

            switch (c0)
            {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += font.width(String.valueOf(c0));

                    if (flag)
                    {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1)
                    {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 'l' && c1 != 'L')
                        {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == '\n')
            {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth)
            {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }

    private static boolean isFormatColor(char colorChar)
    {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

}