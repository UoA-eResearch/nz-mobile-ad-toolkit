package com.adms.australianmobileadtoolkit.interpreter.visual;

import android.graphics.Bitmap;

import java.util.List;

public class Stencil {
   private int[][] stencil;
   private List<Integer> colourPaletteNonWhitespace;

   public Stencil() { }

   public Stencil setStencil(int[][] thisStencil) {
      stencil = thisStencil;
      return this;
   }

   public Stencil setColourPaletteNonWhitespace(List<Integer> thisColourPaletteNonWhitespace) {
      colourPaletteNonWhitespace = thisColourPaletteNonWhitespace;
      return this;
   }

   public int[][] getStencil() {
      return stencil;
   }
   //for debugging


   public List<Integer> getColourPaletteNonWhitespace() {
      return colourPaletteNonWhitespace;
   }
}
