package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.Codebook;
import org.xiph.speex.Encoder;
import org.xiph.speex.Filters;
import org.xiph.speex.Lpc;
import org.xiph.speex.Lsp;
import org.xiph.speex.Misc;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.SbCodec;
import org.xiph.speex.Vbr;

public class SbEncoder extends SbCodec implements Encoder {

   public static final int[] NB_QUALITY_MAP = new int[]{1, 8, 2, 3, 4, 5, 5, 6, 6, 7, 7};
   public static final int[] WB_QUALITY_MAP = new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4};
   public static final int[] UWB_QUALITY_MAP = new int[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
   protected Encoder lowenc;
   private float[] x1d;
   private float[] h0_mem;
   private float[] buf;
   private float[] swBuf;
   private float[] res;
   private float[] target;
   private float[] window;
   private float[] lagWindow;
   private float[] rc;
   private float[] autocorr;
   private float[] lsp;
   private float[] old_lsp;
   private float[] interp_lsp;
   private float[] interp_lpc;
   private float[] bw_lpc1;
   private float[] bw_lpc2;
   private float[] mem_sp2;
   private float[] mem_sw;
   protected int nb_modes;
   private boolean uwb;
   protected int complexity;
   protected int vbr_enabled;
   protected int vad_enabled;
   protected int abr_enabled;
   protected float vbr_quality;
   protected float relative_quality;
   protected float abr_drift;
   protected float abr_drift2;
   protected float abr_count;
   protected int sampling_rate;
   protected int submodeSelect;


   public void wbinit() {
      this.lowenc = new NbEncoder();
      ((NbEncoder)this.lowenc).nbinit();
      super.wbinit();
      this.init(160, 40, 8, 640, 0.9F);
      this.uwb = false;
      this.nb_modes = 5;
      this.sampling_rate = 16000;
   }

   public void uwbinit() {
      this.lowenc = new SbEncoder();
      ((SbEncoder)this.lowenc).wbinit();
      super.uwbinit();
      this.init(320, 80, 8, 1280, 0.7F);
      this.uwb = true;
      this.nb_modes = 2;
      this.sampling_rate = 32000;
   }

   public void init(int var1, int var2, int var3, int var4, float var5) {
      super.init(var1, var2, var3, var4, var5);
      this.complexity = 3;
      this.vbr_enabled = 0;
      this.vad_enabled = 0;
      this.abr_enabled = 0;
      this.vbr_quality = 8.0F;
      this.submodeSelect = this.submodeID;
      this.x1d = new float[var1];
      this.h0_mem = new float[64];
      this.buf = new float[this.windowSize];
      this.swBuf = new float[var1];
      this.res = new float[var1];
      this.target = new float[var2];
      this.window = Misc.window(this.windowSize, var2);
      this.lagWindow = Misc.lagWindow(var3, this.lag_factor);
      this.rc = new float[var3];
      this.autocorr = new float[var3 + 1];
      this.lsp = new float[var3];
      this.old_lsp = new float[var3];
      this.interp_lsp = new float[var3];
      this.interp_lpc = new float[var3 + 1];
      this.bw_lpc1 = new float[var3 + 1];
      this.bw_lpc2 = new float[var3 + 1];
      this.mem_sp2 = new float[var3];
      this.mem_sw = new float[var3];
      this.abr_count = 0.0F;
   }

   public int encode(Bits var1, float[] var2) {
      Filters.qmf_decomp(var2, Codebook.h0, this.x0d, this.x1d, this.fullFrameSize, 64, this.h0_mem);
      this.lowenc.encode(var1, this.x0d);

      int var3;
      for(var3 = 0; var3 < this.windowSize - this.frameSize; ++var3) {
         this.high[var3] = this.high[this.frameSize + var3];
      }

      for(var3 = 0; var3 < this.frameSize; ++var3) {
         this.high[this.windowSize - this.frameSize + var3] = this.x1d[var3];
      }

      System.arraycopy(this.excBuf, this.frameSize, this.excBuf, 0, this.bufSize - this.frameSize);
      float[] var7 = this.lowenc.getPiGain();
      float[] var8 = this.lowenc.getExc();
      float[] var9 = this.lowenc.getInnov();
      int var11 = this.lowenc.getMode();
      boolean var10;
       var10 = var11 == 0;

      for(var3 = 0; var3 < this.windowSize; ++var3) {
         this.buf[var3] = this.high[var3] * this.window[var3];
      }

      Lpc.autocorr(this.buf, this.autocorr, this.lpcSize + 1, this.windowSize);
      ++this.autocorr[0];
      this.autocorr[0] *= this.lpc_floor;

      for(var3 = 0; var3 < this.lpcSize + 1; ++var3) {
         this.autocorr[var3] *= this.lagWindow[var3];
      }

      Lpc.wld(this.lpc, this.autocorr, this.rc, this.lpcSize);
      System.arraycopy(this.lpc, 0, this.lpc, 1, this.lpcSize);
      this.lpc[0] = 1.0F;
      int var12 = Lsp.lpc2lsp(this.lpc, this.lpcSize, this.lsp, 15, 0.2F);
      if(var12 != this.lpcSize) {
         var12 = Lsp.lpc2lsp(this.lpc, this.lpcSize, this.lsp, 11, 0.02F);
         if(var12 != this.lpcSize) {
            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               this.lsp[var3] = (float)Math.cos(3.141592653589793D * (double)((float)(var3 + 1)) / (double)(this.lpcSize + 1));
            }
         }
      }

      for(var3 = 0; var3 < this.lpcSize; ++var3) {
         this.lsp[var3] = (float)Math.acos((double)this.lsp[var3]);
      }

      float var13 = 0.0F;

      for(var3 = 0; var3 < this.lpcSize; ++var3) {
         var13 += (this.old_lsp[var3] - this.lsp[var3]) * (this.old_lsp[var3] - this.lsp[var3]);
      }

      float var15;
      float var16;
      int var32;
      if((this.vbr_enabled != 0 || this.vad_enabled != 0) && !var10) {
         float var14 = 0.0F;
         var15 = 0.0F;
         if(this.abr_enabled != 0) {
            float var17 = 0.0F;
            if(this.abr_drift2 * this.abr_drift > 0.0F) {
               var17 = -1.0E-5F * this.abr_drift / (1.0F + this.abr_count);
               if(var17 > 0.1F) {
                  var17 = 0.1F;
               }

               if(var17 < -0.1F) {
                  var17 = -0.1F;
               }
            }

            this.vbr_quality += var17;
            if(this.vbr_quality > 10.0F) {
               this.vbr_quality = 10.0F;
            }

            if(this.vbr_quality < 0.0F) {
               this.vbr_quality = 0.0F;
            }
         }

         for(var3 = 0; var3 < this.frameSize; ++var3) {
            var14 += this.x0d[var3] * this.x0d[var3];
            var15 += this.high[var3] * this.high[var3];
         }

         var16 = (float)Math.log((double)((1.0F + var15) / (1.0F + var14)));
         this.relative_quality = this.lowenc.getRelativeQuality();
         if(var16 < -4.0F) {
            var16 = -4.0F;
         }

         if(var16 > 2.0F) {
            var16 = 2.0F;
         }

         if(this.vbr_enabled != 0) {
            var32 = this.nb_modes - 1;
            this.relative_quality = (float)((double)this.relative_quality + 1.0D * (double)(var16 + 2.0F));
            if(this.relative_quality < -1.0F) {
               this.relative_quality = -1.0F;
            }

            int var18;
            while(var32 != 0) {
               var18 = (int)Math.floor((double)this.vbr_quality);
               float var19;
               if(var18 == 10) {
                  var19 = Vbr.hb_thresh[var32][var18];
               } else {
                  var19 = (this.vbr_quality - (float)var18) * Vbr.hb_thresh[var32][var18 + 1] + ((float)(1 + var18) - this.vbr_quality) * Vbr.hb_thresh[var32][var18];
               }

               if(this.relative_quality >= var19) {
                  break;
               }

               --var32;
            }

            this.setMode(var32);
            if(this.abr_enabled != 0) {
               var18 = this.getBitRate();
               this.abr_drift += (float)(var18 - this.abr_enabled);
               this.abr_drift2 = 0.95F * this.abr_drift2 + 0.05F * (float)(var18 - this.abr_enabled);
               this.abr_count = (float)((double)this.abr_count + 1.0D);
            }
         } else {
            if((double)this.relative_quality < 2.0D) {
               var32 = 1;
            } else {
               var32 = this.submodeSelect;
            }

            this.submodeID = var32;
         }
      }

      var1.pack(1, 1);
      if(var10) {
         var1.pack(0, 3);
      } else {
         var1.pack(this.submodeID, 3);
      }

      if(!var10 && this.submodes[this.submodeID] != null) {
         this.submodes[this.submodeID].lsqQuant.quant(this.lsp, this.qlsp, this.lpcSize, var1);
         if(this.first != 0) {
            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               this.old_lsp[var3] = this.lsp[var3];
            }

            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               this.old_qlsp[var3] = this.qlsp[var3];
            }
         }

         float[] var4 = new float[this.lpcSize];
         float[] var6 = new float[this.subframeSize];
         float[] var5 = new float[this.subframeSize];

         for(int var31 = 0; var31 < this.nbSubframes; ++var31) {
            float var24 = 0.0F;
            float var25 = 0.0F;
            int var21 = this.subframeSize * var31;
            var32 = this.excIdx + var21;
            int var20 = var21;
            int var33 = var21;
            var15 = (1.0F + (float)var31) / (float)this.nbSubframes;

            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               this.interp_lsp[var3] = (1.0F - var15) * this.old_lsp[var3] + var15 * this.lsp[var3];
            }

            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               this.interp_qlsp[var3] = (1.0F - var15) * this.old_qlsp[var3] + var15 * this.qlsp[var3];
            }

            Lsp.enforce_margin(this.interp_lsp, this.lpcSize, 0.05F);
            Lsp.enforce_margin(this.interp_qlsp, this.lpcSize, 0.05F);

            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               this.interp_lsp[var3] = (float)Math.cos((double)this.interp_lsp[var3]);
            }

            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               this.interp_qlsp[var3] = (float)Math.cos((double)this.interp_qlsp[var3]);
            }

            this.m_lsp.lsp2lpc(this.interp_lsp, this.interp_lpc, this.lpcSize);
            this.m_lsp.lsp2lpc(this.interp_qlsp, this.interp_qlpc, this.lpcSize);
            Filters.bw_lpc(this.gamma1, this.interp_lpc, this.bw_lpc1, this.lpcSize);
            Filters.bw_lpc(this.gamma2, this.interp_lpc, this.bw_lpc2, this.lpcSize);
            float var23 = 0.0F;
            float var22 = 0.0F;
            var15 = 1.0F;
            this.pi_gain[var31] = 0.0F;

            for(var3 = 0; var3 <= this.lpcSize; ++var3) {
               var23 += var15 * this.interp_qlpc[var3];
               var15 = -var15;
               this.pi_gain[var31] += this.interp_qlpc[var3];
            }

            var22 = var7[var31];
            var22 = 1.0F / (Math.abs(var22) + 0.01F);
            var23 = 1.0F / (Math.abs(var23) + 0.01F);
            var16 = Math.abs(0.01F + var23) / (0.01F + Math.abs(var22));
            boolean var26 = var16 < 5.0F;
            var26 = false;
            Filters.fir_mem2(this.high, var21, this.interp_qlpc, this.excBuf, var32, this.subframeSize, this.lpcSize, this.mem_sp2);

            for(var3 = 0; var3 < this.subframeSize; ++var3) {
               var24 += this.excBuf[var32 + var3] * this.excBuf[var32 + var3];
            }

            float var27;
            if(this.submodes[this.submodeID].innovation == null) {
               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  var25 += var9[var21 + var3] * var9[var21 + var3];
               }

               var27 = var24 / (0.01F + var25);
               var27 = (float)Math.sqrt((double)var27);
               var27 *= var16;
               int var28 = (int)Math.floor(10.5D + 8.0D * Math.log((double)var27 + 1.0E-4D));
               if(var28 < 0) {
                  var28 = 0;
               }

               if(var28 > 31) {
                  var28 = 31;
               }

               var1.pack(var28, 5);
               var27 = (float)(0.1D * Math.exp((double)var28 / 9.4D));
               float var10000 = var27 / var16;
            } else {
               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  var25 += var8[var21 + var3] * var8[var21 + var3];
               }

               var27 = (float)(Math.sqrt((double)(1.0F + var24)) * (double)var16 / Math.sqrt((double)((1.0F + var25) * (float)this.subframeSize)));
               int var30 = (int)Math.floor(0.5D + 3.7D * (Math.log((double)var27) + 2.0D));
               if(var30 < 0) {
                  var30 = 0;
               }

               if(var30 > 15) {
                  var30 = 15;
               }

               var1.pack(var30, 4);
               var27 = (float)Math.exp(0.27027027027027023D * (double)var30 - 2.0D);
               float var34 = var27 * (float)Math.sqrt((double)(1.0F + var25)) / var16;
               float var29 = 1.0F / var34;

               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  this.excBuf[var32 + var3] = 0.0F;
               }

               this.excBuf[var32] = 1.0F;
               Filters.syn_percep_zero(this.excBuf, var32, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, var6, this.subframeSize, this.lpcSize);

               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  this.excBuf[var32 + var3] = 0.0F;
               }

               for(var3 = 0; var3 < this.lpcSize; ++var3) {
                  var4[var3] = this.mem_sp[var3];
               }

               Filters.iir_mem2(this.excBuf, var32, this.interp_qlpc, this.excBuf, var32, this.subframeSize, this.lpcSize, var4);

               for(var3 = 0; var3 < this.lpcSize; ++var3) {
                  var4[var3] = this.mem_sw[var3];
               }

               Filters.filter_mem2(this.excBuf, var32, this.bw_lpc1, this.bw_lpc2, this.res, var21, this.subframeSize, this.lpcSize, var4, 0);

               for(var3 = 0; var3 < this.lpcSize; ++var3) {
                  var4[var3] = this.mem_sw[var3];
               }

               Filters.filter_mem2(this.high, var21, this.bw_lpc1, this.bw_lpc2, this.swBuf, var21, this.subframeSize, this.lpcSize, var4, 0);

               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  this.target[var3] = this.swBuf[var33 + var3] - this.res[var20 + var3];
               }

               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  this.excBuf[var32 + var3] = 0.0F;
               }

               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  this.target[var3] *= var29;
               }

               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  var5[var3] = 0.0F;
               }

               this.submodes[this.submodeID].innovation.quant(this.target, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.lpcSize, this.subframeSize, var5, 0, var6, var1, this.complexity + 1 >> 1);

               for(var3 = 0; var3 < this.subframeSize; ++var3) {
                  this.excBuf[var32 + var3] += var5[var3] * var34;
               }

               if(this.submodes[this.submodeID].double_codebook != 0) {
                  float[] var35 = new float[this.subframeSize];

                  for(var3 = 0; var3 < this.subframeSize; ++var3) {
                     var35[var3] = 0.0F;
                  }

                  for(var3 = 0; var3 < this.subframeSize; ++var3) {
                     this.target[var3] = (float)((double)this.target[var3] * 2.5D);
                  }

                  this.submodes[this.submodeID].innovation.quant(this.target, this.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.lpcSize, this.subframeSize, var35, 0, var6, var1, this.complexity + 1 >> 1);

                  for(var3 = 0; var3 < this.subframeSize; ++var3) {
                     var35[var3] = (float)((double)var35[var3] * (double)var34 * 0.4D);
                  }

                  for(var3 = 0; var3 < this.subframeSize; ++var3) {
                     this.excBuf[var32 + var3] += var35[var3];
                  }
               }
            }

            for(var3 = 0; var3 < this.lpcSize; ++var3) {
               var4[var3] = this.mem_sp[var3];
            }

            Filters.iir_mem2(this.excBuf, var32, this.interp_qlpc, this.high, var21, this.subframeSize, this.lpcSize, this.mem_sp);
            Filters.filter_mem2(this.high, var21, this.bw_lpc1, this.bw_lpc2, this.swBuf, var33, this.subframeSize, this.lpcSize, this.mem_sw, 0);
         }

         this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
         this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);

         for(var3 = 0; var3 < this.fullFrameSize; ++var3) {
            var2[var3] = 2.0F * (this.y0[var3] - this.y1[var3]);
         }

         for(var3 = 0; var3 < this.lpcSize; ++var3) {
            this.old_lsp[var3] = this.lsp[var3];
         }

         for(var3 = 0; var3 < this.lpcSize; ++var3) {
            this.old_qlsp[var3] = this.qlsp[var3];
         }

         this.first = 0;
         return 1;
      } else {
         for(var3 = 0; var3 < this.frameSize; ++var3) {
            this.excBuf[this.excIdx + var3] = this.swBuf[var3] = 0.0F;
         }

         for(var3 = 0; var3 < this.lpcSize; ++var3) {
            this.mem_sw[var3] = 0.0F;
         }

         this.first = 1;
         Filters.iir_mem2(this.excBuf, this.excIdx, this.interp_qlpc, this.high, 0, this.subframeSize, this.lpcSize, this.mem_sp);
         this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
         this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);

         for(var3 = 0; var3 < this.fullFrameSize; ++var3) {
            var2[var3] = 2.0F * (this.y0[var3] - this.y1[var3]);
         }

         return var10?0:1;
      }
   }

   public int getEncodedFrameSize() {
      int var1 = SB_FRAME_SIZE[this.submodeID];
      var1 += this.lowenc.getEncodedFrameSize();
      return var1;
   }

   public void setQuality(int var1) {
      if(var1 < 0) {
         var1 = 0;
      }

      if(var1 > 10) {
         var1 = 10;
      }

      if(this.uwb) {
         this.lowenc.setQuality(var1);
         this.setMode(UWB_QUALITY_MAP[var1]);
      } else {
         this.lowenc.setMode(NB_QUALITY_MAP[var1]);
         this.setMode(WB_QUALITY_MAP[var1]);
      }

   }

   public void setVbrQuality(float var1) {
      this.vbr_quality = var1;
      float var2 = var1 + 0.6F;
      if(var2 > 10.0F) {
         var2 = 10.0F;
      }

      this.lowenc.setVbrQuality(var2);
      int var3 = (int)Math.floor(0.5D + (double)var1);
      if(var3 > 10) {
         var3 = 10;
      }

      this.setQuality(var3);
   }

   public void setVbr(boolean var1) {
      this.vbr_enabled = var1?1:0;
      this.lowenc.setVbr(var1);
   }

   public void setAbr(int var1) {
      this.lowenc.setVbr(true);
      this.abr_enabled = var1 != 0?1:0;
      this.vbr_enabled = 1;
      int var2 = 10;

      for(int var4 = var1; var2 >= 0; --var2) {
         this.setQuality(var2);
         int var3 = this.getBitRate();
         if(var3 <= var4) {
            break;
         }
      }

      float var5 = (float)var2;
      if(var5 < 0.0F) {
         var5 = 0.0F;
      }

      this.setVbrQuality(var5);
      this.abr_count = 0.0F;
      this.abr_drift = 0.0F;
      this.abr_drift2 = 0.0F;
   }

   public int getBitRate() {
      return this.submodes[this.submodeID] != null?this.lowenc.getBitRate() + this.sampling_rate * this.submodes[this.submodeID].bits_per_frame / this.frameSize:this.lowenc.getBitRate() + this.sampling_rate * 4 / this.frameSize;
   }

   public void setSamplingRate(int var1) {
      this.sampling_rate = var1;
      this.lowenc.setSamplingRate(var1);
   }

   public int getLookAhead() {
      return 2 * this.lowenc.getLookAhead() + 64 - 1;
   }

   public void setMode(int var1) {
      if(var1 < 0) {
         var1 = 0;
      }

      this.submodeID = this.submodeSelect = var1;
   }

   public int getMode() {
      return this.submodeID;
   }

   public void setBitRate(int var1) {
      for(int var2 = 10; var2 >= 0; --var2) {
         this.setQuality(var2);
         if(this.getBitRate() <= var1) {
            return;
         }
      }

   }

   public boolean getVbr() {
      return this.vbr_enabled != 0;
   }

   public void setVad(boolean var1) {
      this.vad_enabled = var1?1:0;
   }

   public boolean getVad() {
      return this.vad_enabled != 0;
   }

   public void setDtx(boolean var1) {
      this.dtx_enabled = var1?1:0;
   }

   public int getAbr() {
      return this.abr_enabled;
   }

   public float getVbrQuality() {
      return this.vbr_quality;
   }

   public void setComplexity(int var1) {
      if(var1 < 0) {
         var1 = 0;
      }

      if(var1 > 10) {
         var1 = 10;
      }

      this.complexity = var1;
   }

   public int getComplexity() {
      return this.complexity;
   }

   public int getSamplingRate() {
      return this.sampling_rate;
   }

   public float getRelativeQuality() {
      return this.relative_quality;
   }

}
