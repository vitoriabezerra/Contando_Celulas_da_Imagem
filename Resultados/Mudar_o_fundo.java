package pdi20192;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Mudar_o_fundo implements PlugInFilter {
	Random ran = new Random();

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		int h = ip.getHeight();// pega a altura da imagem
		int w = ip.getWidth();// pega a largura da imagem
		Color corbranca = new Color(255, 255, 255); // criando a variavel cor branca
		int branco = corbranca.getRGB();// transforma num inteiro
		int[][] matrizcor = new int[w][h];// cria uma matriz com o tamanho das imagens
		ArrayList<Point> fila = new ArrayList<Point>();
		

		int x=0;
		int y=0;
				
					Color corpreta = new Color(0, 0, 0); // criando a variavel cor preta
					int preto = corpreta.getRGB();
					matrizcor[x][y] = 1;
					ip.set(x, y, preto);
					Point position = new Point(x, y);
					fila.add(position);
					do {
						// VARRENDO OS VIZINHOS
						int xx = fila.get(0).x;
						int yy = fila.get(0).y;
						//int yy = fila.indexOf(0);
						
						for (int x1 = xx - 1; x1 <= xx + 1; x1++) {// Sempre adicionando 1 no eixo x para mais e para
							for (int y1 = yy - 1; y1 <= yy + 1; y1++) {
								if (ip.getPixel(x1, y1) == branco && matrizcor[x1][y1] != 1) {// só irá entrar no loop
																								// se a cor for branca
									position = new Point(x1, y1);// gera a posição para o ponteiro
									fila.add(position);// adiciona os vizinhos na fila
									ip.set(x1, y1, preto);//pinta de preto
									matrizcor[x1][y1] = 1;

								}
							}
						}fila.remove(0);//remove da fila

					} while (fila.isEmpty() == false);
				}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		return DOES_ALL;
	}

}

