package pdi20192;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Labeling implements PlugInFilter {
	Random ran = new Random();
	ArrayList<Point> fila = new ArrayList<Point>();
	ArrayList<Color> ListaDeCores = new ArrayList<Color>();
	HashMap<Integer, ArrayList<Point>> Mapa = new HashMap<Integer, ArrayList<Point>>();
	ArrayList<Point> pontos = new ArrayList<Point>();

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		int h = ip.getHeight();// pega a altura da imagem
		int w = ip.getWidth();// pega a largura da imagem
		Color corpreta = new Color(0, 0, 0); // criando a variavel cor preta
		int preto = corpreta.getRGB();// transforma num inteiro
		int[][] matrizcor = new int[w][h];
		int size = ListaDeCores.size();
		int[] count = new int[size];
		// cria uma matriz com o tamanho das imagens
		// ColorProcessor ip2 = new ColorProcessor(w, h);
		// for (int i = 0; i <= size; i++) {

		for (int x = 0; x < w; x++) {// Varrendo as linhas até w-1
			for (int y = 0; y < h; y++) {// Varrendo as linhas até h-1

				if (ip.getPixel(x, y) == preto && matrizcor[x][y] != 1) {
					matrizcor[x][y] = 1;
					int cor = this.Cor_Random();// chamando a função que gera a cor aleatoria
					ip.set(x, y, cor);
					Point position = new Point(x, y);
					fila.add(position);
					pontos.add(position);
					int contador = 1;

					do {
						// VARRENDO OS VIZINHOS
						int xx = fila.get(0).x;

						int yy = fila.get(0).y;

						for (int x1 = xx - 1; x1 <= xx + 1; x1++) {// Sempre adicionando 1 no eixo x para mais e
																	// para
							for (int y1 = yy - 1; y1 <= yy + 1; y1++) {
								if (ip.getPixel(x1, y1) == preto && matrizcor[x1][y1] != 1) {// só irá entrar no
																								// loop
																								// se a cor for
																								// branca
									position = new Point(x1, y1);// gera a posição para o ponteiro
									fila.add(position);// adiciona os vizinhos na fila
									pontos.add(position);
									ip.set(x1, y1, cor);
									matrizcor[x1][y1] = 1;
									contador++;

								}
							}
						}
						fila.remove(0);

					} while (fila.isEmpty() == false);
					// System.out.println(contador);
					Mapa.put(cor, pontos);
					pontos = new ArrayList<Point>();
					// System.out.println(Mapa.get(cor).size());

				}
			}

		}
		System.out.println("Número de cores: " + ListaDeCores.size());
		/*
		 * System.out.printf("\nPercorrendo o ArrayList usando for-each\n");// printando
		 * as cores utilizadas int j = 0; for (Color cores : ListaDeCores) {
		 * System.out.printf("Posição %d- %s\n", j, cores); j++; }
		 */

	}

	private int Cor_Random() { //método que gera uma cor aleatória (inteiro)

		Random ran = new Random(); 

		int r;
		int g;
		int b;
		Color cor;

		do {
			r = ran.nextInt(255);
			g = ran.nextInt(255);
			b = ran.nextInt(255);

			cor = new Color(r, g, b);

		} while (ListaDeCores.contains(cor) || (r == 255 && b == 255 && g == 255)|| (r == 0 && b == 0 && g == 0));// gera sempre uma nova cor diferente
																					// de branco

		ListaDeCores.add(cor);// adiciona a cor na minha lista de cores
		int rancolor = cor.getRGB();// transforma numa variável do tipo inteira
		return rancolor;// retorna a cor
	}

	public HashMap<Integer, ArrayList<Point>> Exportar_Mapa() {

		return Mapa;
	}


	@Override
	public int setup(String arg0, ImagePlus imp) {
		// TODO Auto-generated method stub
		return DOES_ALL;
	}

}
