package com.hadoop.plat.huffman;
/* 
 * Reference Huffman coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-huffman-coding
 * https://github.com/nayuki/Reference-Huffman-coding
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


/**
 * Decompression application using adaptive Huffman coding.
 * <p>Usage: java AdaptiveHuffmanDecompress InputFile OutputFile</p>
 * <p>This decompresses files generated by the "AdaptiveHuffmanCompress" application.</p>
 */
public final class AdaptiveHuffmanDecompress {
	
	// Command line main application function.
	public static void main(String[] args) throws IOException {
		// Handle command line arguments
		if (args.length != 2) {
			System.err.println("Usage: java AdaptiveHuffmanDecompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		File inputFile  = new File(args[0]);
		File outputFile = new File(args[1]);
		
		// Perform file decompression
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			decompress(in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static void decompress(BitInputStream in, OutputStream out) throws IOException {
		int[] initFreqs = new int[257];
		Arrays.fill(initFreqs, 1);
		
		FrequencyTable freqs = new FrequencyTable(initFreqs);
		HuffmanDecoder dec = new HuffmanDecoder(in);
		dec.codeTree = freqs.buildCodeTree();  // Use same algorithm as the compressor
		int count = 0;  // Number of bytes written to the output file
		while (true) {
			// Decode and write one byte
			int symbol = dec.read();
			if (symbol == 256)  // EOF symbol
				break;
			out.write(symbol);
			count++;
			
			// Update the frequency table and possibly the code tree
			freqs.increment(symbol);
			if (count < 262144 && isPowerOf2(count) || count % 262144 == 0)  // Update code tree
				dec.codeTree = freqs.buildCodeTree();
			if (count % 262144 == 0)  // Reset frequency table
				freqs = new FrequencyTable(initFreqs);
		}
	}
	
	
	private static boolean isPowerOf2(int x) {
		return x > 0 && Integer.bitCount(x) == 1;
	}
	
    /**
     * 对输入数据直接进行解码
     * @param b
     * @return
     * @throws IOException
     */
    public static byte[] decompress(byte[] b) throws IOException {
        InputStream in = new ByteArrayInputStream(b);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AdaptiveHuffmanDecompress.decompress(new BitInputStream(in), out);
        return out.toByteArray();
    }
	
}
