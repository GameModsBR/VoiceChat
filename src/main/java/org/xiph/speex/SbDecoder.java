package org.xiph.speex;

import java.io.StreamCorruptedException;
import org.xiph.speex.Bits;
import org.xiph.speex.Codebook;
import org.xiph.speex.Decoder;
import org.xiph.speex.Filters;
import org.xiph.speex.Lsp;
import org.xiph.speex.NbDecoder;
import org.xiph.speex.SbCodec;
import org.xiph.speex.Stereo;

public class SbDecoder extends SbCodec implements Decoder {

   protected Decoder lowdec;
   protected Stereo stereo = new Stereo();
   protected boolean enhanced = true;
   private float[] innov2;


   public void wbinit() {
      this.lowdec = new NbDecoder();
      ((NbDecoder)this.lowdec).nbinit();
      this.lowdec.setPerceptualEnhancement(this.enhanced);
      super.wbinit();
      this.init(160, 40, 8, 640, 0.7F);
   }

   public void uwbinit() {
      this.lowdec = new SbDecoder();
      ((SbDecoder)this.lowdec).wbinit();
      this.lowdec.setPerceptualEnhancement(this.enhanced);
      super.uwbinit();
      this.init(320, 80, 8, 1280, 0.5F);
   }

   public void init(int var1, int var2, int var3, int var4, float var5) {
      super.init(var1, var2, var3, var4, var5);
      this.excIdx = 0;
      this.innov2 = new float[var2];
   }

   public int decode(Bits var1, float[] var2) throws StreamCorruptedException {
      int var6 = this.lowdec.decode(var1, this.x0d);
      if(var6 != 0) {
         return var6;
      } else {
         boolean var10 = this.lowdec.getDtx();
         if(var1 == null) {
            this.decodeLost(var2, var10);
            return 0;
         } else {
            int var5 = var1.peek();
            if(var5 != 0) {
               var5 = var1.unpack(1);
               this.submodeID = var1.unpack(3);
            } else {
               this.submodeID = 0;
            }

            int var3;
            for(var3 = 0; var3 < this.frameSize; ++var3) {
               this.excBuf[var3] = 0.0F;
            }

            if(this.submodes[this.submodeID] == null) {
               if(var10) {
                  this.decodeLost(var2, true);
                  return 0;
               } else {
                  for(var3 = 0; var3 < this.frameSize; ++var3) {
                     this.excBuf[var3] = 0.0F;
                  }

                  this.first = 1;
                  Filters.iir_mem2(this.excBuf, this.excIdx, this.interp_qlpc, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp);
                  this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
                  this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);

                  for(var3 = 0; var3 < this.fullFrameSize; ++var3) {
                     var2[var3] = 2.0F * (this.y0[var3] - this.y1[var3]);
                  }

                  return 0;
               }
            } else {
               float[] var7 = this.lowdec.getPiGain();
               float[] var8 = this.lowdec.getExc();
               float[] var9 = this.lowdec.getInnov();
               this.submodes[this.submodeID].lsqQuant.unquant(this.qlsp, this.lpcSize, var1);
               if(this.first != 0) {
                  for(var3 = 0; var3 < this.lpcSize; ++var3) {
                     this.old_qlsp[var3] = this.qlsp[var3];
                  }
               }

               for(int var4 = 0; var4 < this.nbSubframes; ++var4) {
                  float var13 = 0.0F;
                  float var14 = 0.0F;
                  float var15 = 0.0F;
                  int var16 = this.subframeSize * var4;
                  float var11 = (1.0F + (float)var4) / (float)this.nbSubframes;

                  for(var3 = 0; var3 < this.lpcSize; ++var3) {
                     this.interp_qlsp[var3] = (1.0F - var11) * this.old_qlsp[var3] + var11 * this.qlsp[var3];
                  }

                  Lsp.enforce_margin(this.interp_qlsp, this.lpcSize, 0.05F);

                  for(var3 = 0; var3 < this.lpcSize; ++var3) {
                     this.interp_qlsp[var3] = (float)Math.cos((double)this.interp_qlsp[var3]);
                  }

                  this.m_lsp.lsp2lpc(this.interp_qlsp, this.interp_qlpc, this.lpcSize);
                  float var17;
                  float var18;
                  if(this.enhanced) {
                     var17 = this.submodes[this.submodeID].lpc_enh_k1;
                     var18 = this.submodes[this.submodeID].lpc_enh_k2;
                     float var19 = var17 - var18;
                     Filters.bw_lpc(var17, this.interp_qlpc, this.awk1, this.lpcSize);
                     Filters.bw_lpc(var18, this.interp_qlpc, this.awk2, this.lpcSize);
                     Filters.bw_lpc(var19, this.interp_qlpc, this.awk3, this.lpcSize);
                  }

                  var11 = 1.0F;
                  this.pi_gain[var4] = 0.0F;

                  for(var3 = 0; var3 <= this.lpcSize; ++var3) {
                     var15 += var11 * this.interp_qlpc[var3];
                     var11 = -var11;
                     this.pi_gain[var4] += this.interp_qlpc[var3];
                  }

                  var14 = var7[var4];
                  var14 = 1.0F / (Math.abs(var14) + 0.01F);
                  var15 = 1.0F / (Math.abs(var15) + 0.01F);
                  float var12 = Math.abs(0.01F + var15) / (0.01F + Math.abs(var14));

                  for(var3 = var16; var3 < var16 + this.subframeSize; ++var3) {
                     this.excBuf[var3] = 0.0F;
                  }

                  if(this.submodes[this.submodeID].innovation == null) {
                     int var20 = var1.unpack(5);
                     var17 = (float)Math.exp(((double)var20 - 10.0D) / 8.0D);
                     var17 /= var12;

                     for(var3 = var16; var3 < var16 + this.subframeSize; ++var3) {
                        this.excBuf[var3] = this.foldingGain * var17 * var9[var3];
                     }
                  } else {
                     int var21 = var1.unpack(4);

                     for(var3 = var16; var3 < var16 + this.subframeSize; ++var3) {
                        var13 += var8[var3] * var8[var3];
                     }

                     var17 = (float)Math.exp((double)(0.27027026F * (float)var21 - 2.0F));
                     var18 = var17 * (float)Math.sqrt((double)(1.0F + var13)) / var12;
                     this.submodes[this.submodeID].innovation.unquant(this.excBuf, var16, this.subframeSize, var1);

                     for(var3 = var16; var3 < var16 + this.subframeSize; ++var3) {
                        this.excBuf[var3] *= var18;
                     }

                     if(this.submodes[this.submodeID].double_codebook != 0) {
                        for(var3 = 0; var3 < this.subframeSize; ++var3) {
                           this.innov2[var3] = 0.0F;
                        }

                        this.submodes[this.submodeID].innovation.unquant(this.innov2, 0, this.subframeSize, var1);

                        for(var3 = 0; var3 < this.subframeSize; ++var3) {
                           this.innov2[var3] *= var18 * 0.4F;
                        }

                        for(var3 = 0; var3 < this.subframeSize; ++var3) {
                           this.excBuf[var16 + var3] += this.innov2[var3];
                        }
                     }
                  }

                  for(var3 = var16; var3 < var16 + this.subframeSize; ++var3) {
                     this.high[var3] = this.excBuf[var3];
                  }

                  if(this.enhanced) {
                     Filters.filter_mem2(this.high, var16, this.awk2, this.awk1, this.subframeSize, this.lpcSize, this.mem_sp, this.lpcSize);
                     Filters.filter_mem2(this.high, var16, this.awk3, this.interp_qlpc, this.subframeSize, this.lpcSize, this.mem_sp, 0);
                  } else {
                     for(var3 = 0; var3 < this.lpcSize; ++var3) {
                        this.mem_sp[this.lpcSize + var3] = 0.0F;
                     }

                     Filters.iir_mem2(this.high, var16, this.interp_qlpc, this.high, var16, this.subframeSize, this.lpcSize, this.mem_sp);
                  }
               }

               this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
               this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);

               for(var3 = 0; var3 < this.fullFrameSize; ++var3) {
                  var2[var3] = 2.0F * (this.y0[var3] - this.y1[var3]);
               }

               for(var3 = 0; var3 < this.lpcSize; ++var3) {
                  this.old_qlsp[var3] = this.qlsp[var3];
               }

               this.first = 0;
               return 0;
            }
         }
      }
   }

   public int decodeLost(float[] var1, boolean var2) {
      int var4 = 0;
      if(var2) {
         var4 = this.submodeID;
         this.submodeID = 1;
      } else {
         Filters.bw_lpc(0.99F, this.interp_qlpc, this.interp_qlpc, this.lpcSize);
      }

      this.first = 1;
      this.awk1 = new float[this.lpcSize + 1];
      this.awk2 = new float[this.lpcSize + 1];
      this.awk3 = new float[this.lpcSize + 1];
      if(this.enhanced) {
         float var5;
         float var6;
         if(this.submodes[this.submodeID] != null) {
            var5 = this.submodes[this.submodeID].lpc_enh_k1;
            var6 = this.submodes[this.submodeID].lpc_enh_k2;
         } else {
            var6 = 0.7F;
            var5 = 0.7F;
         }

         float var7 = var5 - var6;
         Filters.bw_lpc(var5, this.interp_qlpc, this.awk1, this.lpcSize);
         Filters.bw_lpc(var6, this.interp_qlpc, this.awk2, this.lpcSize);
         Filters.bw_lpc(var7, this.interp_qlpc, this.awk3, this.lpcSize);
      }

      int var3;
      if(!var2) {
         for(var3 = 0; var3 < this.frameSize; ++var3) {
            this.excBuf[this.excIdx + var3] = (float)((double)this.excBuf[this.excIdx + var3] * 0.9D);
         }
      }

      for(var3 = 0; var3 < this.frameSize; ++var3) {
         this.high[var3] = this.excBuf[this.excIdx + var3];
      }

      if(this.enhanced) {
         Filters.filter_mem2(this.high, 0, this.awk2, this.awk1, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp, this.lpcSize);
         Filters.filter_mem2(this.high, 0, this.awk3, this.interp_qlpc, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp, 0);
      } else {
         for(var3 = 0; var3 < this.lpcSize; ++var3) {
            this.mem_sp[this.lpcSize + var3] = 0.0F;
         }

         Filters.iir_mem2(this.high, 0, this.interp_qlpc, this.high, 0, this.frameSize, this.lpcSize, this.mem_sp);
      }

      this.filters.fir_mem_up(this.x0d, Codebook.h0, this.y0, this.fullFrameSize, 64, this.g0_mem);
      this.filters.fir_mem_up(this.high, Codebook.h1, this.y1, this.fullFrameSize, 64, this.g1_mem);

      for(var3 = 0; var3 < this.fullFrameSize; ++var3) {
         var1[var3] = 2.0F * (this.y0[var3] - this.y1[var3]);
      }

      if(var2) {
         this.submodeID = var4;
      }

      return 0;
   }

   public void decodeStereo(float[] var1, int var2) {
      this.stereo.decode(var1, var2);
   }

   public void setPerceptualEnhancement(boolean var1) {
      this.enhanced = var1;
   }

   public boolean getPerceptualEnhancement() {
      return this.enhanced;
   }
}
