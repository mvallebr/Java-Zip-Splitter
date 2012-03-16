/*
   
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */

/**
 * This program can split any binary file into several TXT files, so you can 
 * send them by email. It also can be used to join the TXT files back to the 
 * original bin file
 * */

package br.com.fabiopereira.quebrazip;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

public class JavaZipSplitter {
	private static String BIN_PATH = "./a.zip";
	private static String ORIGEM_PATH = "./";
	private static int bytesSize = (int) (2.5 * 1024 * 1024);

	public static void help(){
		System.out.println("========================================================");
		System.out.println("=================> How to use: <========================");
		System.out.println("To Split a bin file in several TXT files:");
		System.out.println("JavaZipSplitter -sizeinbytes=2621440 -split -bin /path/filename.zip");
		System.out.println("To join TXT files in an origin folder into a bin file:");
		System.out.println("JavaZipSplitter -sizeinbytes=2621440 -join -origin /path/");
		System.out.println("========================================================");
	}
	
	public static void main(String[] args) {
		help();
		boolean split=true;
		boolean zip_path=false, origin_path=false;
		boolean sizeinbytes=false;
		for (String arg : args){
			if (arg.trim().equalsIgnoreCase("-join"))
				split=false;
			if (zip_path){
				BIN_PATH=arg;
				zip_path=false;
			}
			if (origin_path){
				ORIGEM_PATH=arg;
				origin_path=false;
			}
			if (sizeinbytes){
				bytesSize = Integer.parseInt(arg);
				sizeinbytes=false;
			}
			if (arg.trim().equalsIgnoreCase("-bin"))
				zip_path=true;
			if (arg.trim().equalsIgnoreCase("-origin"))
				origin_path=true;
			if (arg.trim().equalsIgnoreCase("-sizeinbytes"))
				sizeinbytes=true;	
		}
		if (zip_path || origin_path || sizeinbytes){
			System.err.println("Error: invalid use of arguments");
			return;
		}
			
		if (split) {
			System.out.println("Spliting zip '"+BIN_PATH+"'");
			split();
		} else {
			System.out.println("Joining files at '"+ORIGEM_PATH+"'");
			join();
		}
	}

	private static void split() {
		try {
			byte[] bytes = FileUtils.readFileToByteArray(new File(BIN_PATH));
			// Número de arquivos inteiros do tamanho bytesSize
			int numFilesInteiros = (bytes.length / bytesSize);
			// Calculando a sobra...(ultimo arquivo)
			int kByteSizeUltimoArquivo = bytes.length - bytesSize
					* numFilesInteiros;
			System.out
					.println("Número de arquivos que serão gerados: "
							+ (numFilesInteiros + (kByteSizeUltimoArquivo != 0 ? 1
									: 0)));
			System.out.println("Tamanhos: ");
			for (int i = 0; i < numFilesInteiros; i++) {
				System.out.println("File " + i + ": " + bytesSize);
			}
			System.out.println("File " + numFilesInteiros + ": "
					+ kByteSizeUltimoArquivo);
			// Escrevendo arquivos inteiros
			byte[][] b = new byte[numFilesInteiros][bytesSize];
			int posicaoInicial = 0;
			for (int i = 0; i < numFilesInteiros; i++) {
				// Um arquivo inteiro
				int j = 0;
				for (j = posicaoInicial; j < posicaoInicial + bytesSize; j++) {
					b[i][j - posicaoInicial] = (bytes[j]);
				}
				posicaoInicial = j;
				invertArrayBytes(b[i]);
				FileUtils.writeByteArrayToFile(
						new File(BIN_PATH.replaceAll(".zip", ".txt")
								+ (i < 10 ? "0" + i : i)), b[i]);
			}
			// Escrevendo ultimo arquivo
			if (kByteSizeUltimoArquivo > 0) {
				byte[] ultimoArquivo = new byte[kByteSizeUltimoArquivo];
				for (int i = posicaoInicial; i < posicaoInicial
						+ kByteSizeUltimoArquivo; i++) {
					ultimoArquivo[i - posicaoInicial] = (bytes[i]);
				}
				invertArrayBytes(ultimoArquivo);
				FileUtils
						.writeByteArrayToFile(
								new File(BIN_PATH.replaceAll(".zip", ".txt")
										+ (numFilesInteiros < 10 ? "0"
												+ numFilesInteiros
												: numFilesInteiros)),
								ultimoArquivo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void join() {
		try {
			// Escrevendo para simular
			File folder = new File(ORIGEM_PATH);
			File[] files = folder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return !file.getName().endsWith(".zip");
				}
			});
			List<File> fileList = new ArrayList<File>();
			for (File file : files) {
				fileList.add(file);
			}
			Collections.sort(fileList, new Comparator<File>() {
				public int compare(File f1, File f2) {
					int i1 = Integer.parseInt(f1.getName().substring(
							f1.getName().indexOf(".txt") + 4));
					int i2 = Integer.parseInt(f2.getName().substring(
							f2.getName().indexOf(".txt") + 4));
					if (i1 == i2) {
						return 0;
					}
					return i1 < i2 ? -1 : 1;
				};
			});
			// Obtendo tamanho total
			int sizeTotal = 0;
			for (File file : fileList) {
				sizeTotal += file.length();
			}
			byte[] tudao = new byte[sizeTotal];
			int posicao = 0;
			for (File file : fileList) {
				byte[] b = FileUtils.readFileToByteArray(file);
				invertArrayBytes(b);
				int i = 0;
				for (i = posicao; i < posicao + b.length; i++) {
					tudao[i] = (b[i - posicao]);
				}
				posicao = i;
			}
			FileUtils.writeByteArrayToFile(new File(ORIGEM_PATH
					+ "/result.zip"), tudao);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void invertArrayBytes(byte[] bytes) {
		Object[] objs = new Object[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			objs[i] = Byte.valueOf(bytes[i]);
		}
		CollectionUtils.reverseArray(objs);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (Byte) objs[i];
		}
	}
}