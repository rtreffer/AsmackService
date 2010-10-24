package com.googlecode.asmack.connection.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/*
 * This patch popped up in the tomcat bugtracker
 * https://issues.apache.org/bugzilla/show_bug.cgi?id=48738
 * contributed by linkedin, and should thus be Apache Licenced.
 *
 * It solves the gzip flush problem as described at
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4255743
 *
 * The patch is smaller, easier and faster than the jzlib option.
 *
 * Altered to use Deflater.BEST_COMPRESSION instead of DEFAULT_COMPRESSION.
 * 
 */

/**
 * User: Jiong Wang (jiwang@linkedin.com)
 * Date: Jan 6, 2010
 * Time: 10:30:48 AM
 *
 */
public class FlushableGZIPOutputStream extends GZIPOutputStream
 {
   public FlushableGZIPOutputStream(OutputStream os) throws IOException
   {
     super(os);
     def.setLevel(Deflater.BEST_COMPRESSION);
   }

   private static final byte[] EMPTYBYTEARRAY = new byte[0];
   private boolean hasData = false;

   /**
    * Here we make sure we have received data, so that the header has
    * been for sure written to the output stream already.
    */
   @Override
   public synchronized void write(byte[] bytes, int i, int i1) throws IOException
   {
     super.write(bytes, i, i1);
     hasData = true;
   }

   @Override
   public synchronized void write(int i) throws IOException
   {
     super.write(i);
     hasData = true;
   }

   @Override
   public synchronized void write(byte[] bytes) throws IOException
   {
     super.write(bytes);
     hasData = true;
   }

   @Override
   public synchronized void flush() throws IOException
   {
     if (!hasData)
     {
       return; // do not allow the gzip header to be flushed on its own
     }

     // trick the deflater to flush
     /**
      * Now this is tricky: We force the Deflater to flush its data by
      * switching compression level. As yet, a perplexingly simple workaround
      * for http://developer.java.sun.com/developer/bugParade/bugs/4255743.html
      */
     if (!def.finished()) {
       def.setInput(EMPTYBYTEARRAY, 0, 0);

       def.setLevel(Deflater.NO_COMPRESSION);
       deflate();

       def.setLevel(Deflater.BEST_COMPRESSION);
       deflate();

       out.flush();
     }

     hasData = false; // no more data to flush 
   }

   /*
    * Keep on calling deflate until it runs dry. The default implementation only does it once and can therefore
    * hold onto data when they need to be flushed out.
    */
  protected void deflate() throws IOException
  {
    int len;
    do
    {
      len = def.deflate(buf, 0, buf.length);
      if (len > 0)
      {
        out.write(buf, 0, len);
      }
    }
    while (len != 0);
  }

 }