/* Copyright (c) 2026 XHYN_PH. SPDX-License-Identifier: MIT */
package com.naruto.r2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Small status files only; never opens a game save or shader-cache file. */
public final class SessionFiles {
    public static void write(Path target, String text) throws IOException {
        Path temp=Files.createTempFile(target.getParent(),"storm-status-",".tmp");
        try {
            Files.write(temp,text.getBytes(StandardCharsets.UTF_8));
            try { Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
    public static String read(Path file,int limit,boolean tail) throws IOException {
        if(limit<0||limit>131072)throw new IllegalArgumentException("Invalid status-file bound");
        if(Files.isSymbolicLink(file)||!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS))return "";
        try(RandomAccessFile input=new RandomAccessFile(file.toFile(),"r")) {
            int count=(int)Math.min(limit,input.length());
            if(tail)input.seek(input.length()-count);
            byte[] data=new byte[count];input.readFully(data);
            return new String(data,StandardCharsets.UTF_8);
        }
    }
    public static boolean gameIdle(Path appFiles) throws IOException {
        try(RandomAccessFile file=new RandomAccessFile(appFiles.resolve("r2-game.lock").toFile(),"rw")) {
            try(FileLock lock=file.getChannel().tryLock()) { return lock!=null; }
            catch(OverlappingFileLockException heldInThisProcess) { return false; }
        }
    }
}
