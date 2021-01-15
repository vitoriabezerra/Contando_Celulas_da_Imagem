package pdi20192;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;

import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class Region_Growing_Grupo_05 implements PlugInFilter {
	ImagePlus imp;// Criando ImagePlus para a imagem original
	ImagePlus imagem_RGB;// Criando ImagePlus para a imagem que será RGB para aplicar o Labeling
	ImagePlus imagem_AUX;// Criando ImagePlus para a imagem auxiliar para achar o perimetro
	ImagePlus imagemFinal;// Criando ImagePlus para a imagem sem os falsos positivos
	ImageProcessor ip1;
	ImageProcessor ip2;
	ImageProcessor ip3;

	HashMap<Integer, ArrayList<Point>> Mapa = new HashMap<Integer, ArrayList<Point>>();// HashMap - Chave (cor) - Valor
																						// (lista de pontos)

	// Criando listas para guardar as estatatísticas de cada um dos candidatos a
	// embrião.
	ArrayList<Color> ListaDeCores = new ArrayList<Color>();
	ArrayList<Double> area1 = new ArrayList<Double>();
	ArrayList<Point> pontos = new ArrayList<Point>();
	ArrayList<Point> centroide = new ArrayList<Point>();
	ArrayList<Integer> borda = new ArrayList<Integer>();
	ArrayList<Double> perimetro = new ArrayList<Double>();
	ArrayList<Double> circularidade = new ArrayList<Double>();
	ArrayList<Double> intensidademedia = new ArrayList<Double>();
	ArrayList<Double> intensidademodal = new ArrayList<Double>();
	ArrayList<Double> intensidademediana = new ArrayList<Double>();
	ArrayList<Double> intensidademinima = new ArrayList<Double>();
	ArrayList<Integer> intensidademaxima = new ArrayList<Integer>();

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		boolean ola = ip.isGrayscale();// retorna uma variavel booleana true se a a imagem for 8bit
		if (ola == false) { // Caso a imagem não seja 8bit grayscale, o programa exibe uma mensagem de erro
							// e fecha.
			GenericDialog box = new GenericDialog("ERRO");
			box.addMessage("A imagem não é em escala de cinza");
			box.showDialog();
			System.exit(1);
		}
		// Pegando as informações da ROI;
		WaitForUserDialog caixa = new WaitForUserDialog("Selecionar ROI", "Selecione uma ROI do pano de fundo");
		caixa.show();
		Roi r = imp.getRoi();
		// Usando a classe getStatistics, é possivel obter as estatisticas abaixo
		double area = r.getStatistics().area;
		double min = r.getStatistics().min;
		double max = r.getStatistics().max;
		double media = r.getStatistics().mean;
		double moda = r.getStatistics().mode;
		double mediana = r.getStatistics().median;
		double desviop = r.getStatistics().stdDev;
		double variancia = desviop * desviop;
		String tipoderoi = r.getTypeAsString();
		double posicaox = r.getXBase();
		double posicaoy = r.getYBase();
		double larg = r.getStatistics().roiWidth;
		double alt = r.getStatistics().roiHeight;

		// Pegando as informações da imagem original para criar uma imagem RGB de mesmo
		// tamanho, a fim de possibilitar a utilização do labeling
		int h = ip.getHeight();// Pegando a altura da imagem original
		int w = ip.getWidth();// Pegando a largura da imagem original
		imagem_RGB = NewImage.createRGBImage("Imagem Binária", w, h, 1, NewImage.FILL_WHITE);
		ip1 = imagem_RGB.getProcessor();// Associando a imagem a um novo get processor

		// declarando as cores preta e branca para a binarização da imagem
		Color branca = new Color(255, 255, 255);
		int BRANCA = branca.getRGB();
		Color preta = new Color(0, 0, 0);
		int PRETA = preta.getRGB();

		// o proximo for varre a imagem e aplica uma adaptação da condição de
		// intensidade dada no
		// documento do lab para diferenciar pano de fundo e candidatos a embrião
		// utilizamos a variancia no lugar do desvio padrão, pois vimos que o resultado
		// na imagem era melhor
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				int temp = ip.get(x, y);// pega os valores do pixel da primeira imagem
				double valor = temp - media;// valor de intensidade (x,y)- intensidade meddia da roi
				double valor2 = Math.abs(valor);// pegamos o valor absoluto devido a formula

				if (valor2 <= (3 * variancia)) {
					ip1.set(x, y, BRANCA);// pinta o pixel de branco -apenas na segunda imagem -
				} else {
					ip1.set(x, y, PRETA);// pinta o pixel de branco -apenas na segunda imagem -

				}
			}
		}

		ImagePlus imagem_binaria = imagem_RGB.duplicate();// Duplicando a imagem antes de aplicar o labeling
		imagem_binaria.show();// Mostrando a imagem
		Labeling fl = new Labeling();// chamando a função;
		fl.run(ip1); // aplicando o labeling no ip da imagem binarizada
		imagem_RGB.updateAndDraw();// atualiza a imagem
		imagem_RGB.show();// mostra a imagem RGB com o labeling.
		Mapa = fl.Exportar_Mapa(); // chama a função e exporta o mapa gerado no labeling para a classe atual
		imagem_AUX = imagem_RGB.duplicate();// duplicando a imagem colorida para auxiliar na obtenção dos valores de
											// perímetro
		ip2 = imagem_AUX.getProcessor();// associa a imagem a um imageprocessor
		// imagem_AUX.show(); //a imagem não será mostrada pois não é necessário
		imagemFinal = imagem_RGB.duplicate();// gerando a última imagem. foram aplicadas os critérios para determinação
												// e exclusão dos falsos positivos
		ip3 = imagemFinal.getProcessor();// associando a imagem final a um ImageProcessor

		Set<Integer> chaves = Mapa.keySet();
		for (Iterator iterator = chaves.iterator(); iterator.hasNext();) {
			Integer corAtual = (Integer) iterator.next();
			ArrayList<Point> arrayAtual = Mapa.get(corAtual);
			// System.out.println(new Color(corAtual).toString()+"\t
			// tamanho:"+arrayAtual.size());
			for (Iterator iterator2 = arrayAtual.iterator(); iterator2.hasNext();) {
				Point point = (Point) iterator2.next();
				// System.out.println("x: "+point.x+"\t y: "+point.y);

			}
			// nas próximas linhas, os métodos criados para calcular as estatísticas de cada
			// embrião são chamados
			double size = arrayAtual.size();
			this.Calcula_Area(size);
			this.Calcula_Centroide(arrayAtual);
			this.AnalisaBorda(h, w, arrayAtual);
			this.Calcula_Perimetro(arrayAtual, h, w, ip2);
			double areaaux1 = area1.get((area1.size()) - 1);// variável auxiliar para pegar a última área calculada
			double peraux1 = perimetro.get((perimetro.size()) - 1);// variável auxiliar para pegar o último perímetro
																	// calculado
			this.Calcula_Circularidade(areaaux1, peraux1);
			this.Calcula_Intensidade_Media(arrayAtual, ip);
			this.Calcula_Intensidade_Modal(arrayAtual, ip);
			this.Calcula_Intensidade_Mediana(arrayAtual, ip);
			this.Calcula_Minimo(arrayAtual, ip);
			this.Calcula_Maximo(arrayAtual, ip);
			this.Exclui_Area(areaaux1, arrayAtual, ip3);
			double bordaaux1 = borda.get((borda.size()) - 1);
			this.Exclui_Borda(bordaaux1, arrayAtual, ip3);

		}
		// imprimindo na tela as estatísticas calculadas acima

		/*
		 * System.out.println("Area:" + area1);
		 * System.out.println("Número de areas calculadas:" + area1.size());
		 * System.out.println("Pontos do Centroide:" + centroide);
		 * System.out.println("Número de centroides calculados:" + centroide.size());
		 * System.out.println("Borda:" + borda);
		 * System.out.println("Número de bordas analisadas:" + borda.size());
		 * System.out.println("Perimetro:" + perimetro);
		 * System.out.println("Número de perimetro analisadas:" + perimetro.size());
		 * System.out.println("Circularidade" + circularidade);
		 * System.out.println("Número de circularidades analisadas:" +
		 * circularidade.size()); System.out.println("Media da intensidade" +
		 * intensidademedia);
		 * System.out.println("Número de intensidade medias analisadas:" +
		 * intensidademedia.size()); System.out.println("Moda da intensidade" +
		 * intensidademodal);
		 * System.out.println("Número de intensidade modais analisadas:" +
		 * intensidademodal.size()); System.out.println("Mediana da intensidade" +
		 * intensidademediana);
		 * System.out.println("Número de intensidade medianas analisadas:" +
		 * intensidademediana.size()); System.out.println("Minimo da intensidade" +
		 * intensidademinima);
		 * System.out.println("Número de intensidade minimas analisadas:" +
		 * intensidademinima.size()); System.out.println("Máximo da intensidade" +
		 * intensidademaxima);
		 * System.out.println("Número de intensidade máximas analisadas:" +
		 * intensidademaxima.size());
		 */
		imagemFinal.show(); // mostrando a imagem final
		this.tabelaresumo();// aplicando o método que gera a tabela de resultados
		//
		// as linhas abaixo criam uma tabela de resultados para as estatísticas da ROI
		ResultsTable tabeladaroi = new ResultsTable();
		// Adiciona os valores na tabela de resultados da Roi
		tabeladaroi.incrementCounter();
		tabeladaroi.addValue("Forma da ROI", "" + tipoderoi + "");
		tabeladaroi.addValue("Área", area);
		tabeladaroi.addValue("Intensidade mínima", min);
		tabeladaroi.addValue("Intensidade máxima", max);
		tabeladaroi.addValue("Intensidade média", media);
		tabeladaroi.addValue("Intensidade modal", moda);
		tabeladaroi.addValue("Intensidade mediana", mediana);
		tabeladaroi.addValue("Desvio Padrão", desviop);
		tabeladaroi.addValue("Variância", variancia);
		tabeladaroi.addValue("Posição Inicial", "(" + posicaox + "," + posicaoy + ")");
		tabeladaroi.addValue("Largura", larg);
		tabeladaroi.addValue("Altura", alt);
		tabeladaroi.show("Tabela de Estatísticas da ROI"); // imprimindo a tabela na tela e determinando seu título

	}

	private ArrayList<Double> Calcula_Area(double size) { // método que calcula a área de cada embrião e adicionando os
															// valores numa lista

		area1.add(size);
		return area1;
	}

	private ArrayList<Point> Calcula_Centroide(ArrayList<Point> listaaux) {// método que adiciona as coordenadas do
																			// centroide em uma lista

		double centroXsoma = 0;
		double centroYsoma = 0;
		double value;
		double centroX;
		double centroY;

		for (int a = 0; a < listaaux.size(); a++) {// varrendo a lista auxiliar e somando todas as coordenadas x e todas
													// as coordenadas y
			centroXsoma = centroXsoma + listaaux.get(a).x;
			centroYsoma = centroYsoma + listaaux.get(a).y;
		}
		value = (double) listaaux.size();// guardando a quantidade de pontos varridos num inteiro
											// nas próximas linhas a definição de centroide é aplicada
		centroX = (centroXsoma / value);// dividindo o somatório de coordenadas x pela quantidade total de coordenadas
		centroY = (centroYsoma / value);// dividindo o somatório de coordenadas y pela quantidade total de coordenadas
		Point position = new Point((int) centroX, (int) centroY); // criando um novo ponto a partir das coordenadas do
																	// centroide
		centroide.add(position); // adicionando cada ponto de centroide a uma lista

		return centroide;
	}

	private ArrayList<Integer> AnalisaBorda(int h, int w, ArrayList<Point> listaaux) {
		int x;
		int y;
		int value = 0;
		for (int a = 0; a < listaaux.size(); a++) {
			x = listaaux.get(a).x;
			y = listaaux.get(a).y;
			if (x == 0 || x == w - 1 || y == 0 || y == h - 1) {// se estiver nos limites da imagem, o valorp muda para 1
				value = 1;

			}

		}
		borda.add(value);// Adiciona os valores (0 ou 1) na lista
		return borda;
	}

	private ArrayList<Double> Calcula_Perimetro(ArrayList<Point> listaaux, int h, int w, ImageProcessor ip2) {
		Mudar_o_fundo mf = new Mudar_o_fundo(); // chama função pintar o fundo
		mf.run(ip2);// aplica a mudança de fundo na imagem_auxiliar
		int contador = 0;
		double per = 0;
		Color corpreta = new Color(0, 0, 0); // criando a variavel cor branca
		int preto = corpreta.getRGB();// transforma num inteiro

		for (int a = 0; a < listaaux.size(); a++) {
			int x = listaaux.get(a).x;// pega o valor x
			int y = listaaux.get(a).y;// pega o valor y
			contador = 0;
			for (int x1 = x - 1; x1 <= x + 1; x1++) {// for utilizado para varrer a vizinhança
				for (int y1 = y - 1; y1 <= y + 1; y1++) {
					if (ip2.getPixel(x1, y1) == preto) {// se um pixel da vizinhança for preto, a unidade é somada aoa
														// contador
						contador++;

					}

				}

			}
			if (contador > 0) {// se o contador não for nulo, ou seja, se o pixel tiver um ou mais vizinhos
								// pretos, ele é perímetro
				per++; // o pixel é perímetro, então somamos a unidade à variável que representa o
						// perímetro.
			}
		}
		perimetro.add(per);// o valor do perímetro obtido é adicionado à lista de perímetros

		return perimetro;

	}

	private ArrayList<Double> Calcula_Circularidade(double area, double perimetro) {// método que será utilizado para
																					// calcular a circularidade do
																					// candidato
		double circ = (4 * Math.PI * area) / (perimetro * perimetro);// aplicando a definição de circularidade

		circularidade.add(circ);// adicionando o valor de circularidade obtido à lista de circularidades
		return circularidade;
	}

	private ArrayList<Double> Calcula_Minimo(ArrayList<Point> listaaux, ImageProcessor ip) {// método que calcula a
																							// intensidade mínima do
																							// candidato a partir da
																							// imagem original
		int x;
		int y;
		double min = 1000;// inicializando a variável que receberá a intensidade mínima com um valor
							// nitidamente maior que qualquer uma das intensidades do pixel
		for (int a = 0; a < listaaux.size(); a++) {// varrendo a lista de pontos
			x = listaaux.get(a).x;// atribuindo o valor da coordenada x a um inteiro
			y = listaaux.get(a).y;// atribuindo o valor da coordenada y a um inteiro
			int temp = ip.get(x, y);// variável inteira temporária que guarda a intensidade do pixel
			if (temp < min) {// aplicando condição para pegar o menor dos valores de intensidade
				min = temp;
			}

		}
		intensidademinima.add(min);// adicionando a intensidade mínima à lista de intensidades
		return intensidademinima;
	}

	private ArrayList<Integer> Calcula_Maximo(ArrayList<Point> listaaux, ImageProcessor ip) {// método que calcula a
																								// intensidade mínima do
																								// candidato a partir da
																								// imagem original
		int x;
		int y;
		int max = -1000;// inicializando a variável que receberá a intensidade máxima com um valor
						// nitidamente menor que qualquer uma das intensidades do pixel
		for (int a = 0; a < listaaux.size(); a++) {// varrendo a lista de pontos
			x = listaaux.get(a).x;// atribuindo o valor da coordenada x a um inteiro
			y = listaaux.get(a).y;// atribuindo o valor da coordenada y a um inteiro
			int temp = ip.get(x, y);// variável inteira temporária que guarda a intensidade do pixel
			if (temp > max) {// aplicando condição para pegar a maior dos valores de intensidade
				max = temp;
			}

		}
		intensidademaxima.add(max);// adicionando a intensidade mínima à lista de intensidades
		return intensidademaxima;
	}

	private ArrayList<Double> Calcula_Intensidade_Media(ArrayList<Point> listaaux, ImageProcessor ip) {// método que
																										// calcula a
																										// intensidade
																										// média do
																										// candidato a
																										// partir da
																										// imagem
																										// original
		double soma = 0; // variável que vai receber o somatório das intensidades
		double media = 0; // variável que vai receber o valor da média
		for (int a = 0; a < listaaux.size(); a++) {// varrendo a lista de pontos
			int x = listaaux.get(a).x;
			int y = listaaux.get(a).y;
			int temp = ip.get(x, y);
			soma = soma + temp; // realizando o somatório

		}
		media = soma / listaaux.size();// dividindo o somatório pela quantidade de pontos para obtenção da média
		intensidademedia.add(media);
		return intensidademedia;
	}

	private ArrayList<Double> Calcula_Intensidade_Modal(ArrayList<Point> listaaux, ImageProcessor ip) {// método que
																										// calcula a
																										// intensidade
																										// modal do
																										// candidato a
																										// partir da
																										// imagem
																										// original
		int x;
		int y;
		int temp;
		double moda = 0;
		int[] vetorcont = new int[256];// declarando um vetor do tamanho da quantidade de intensidades existentes

		for (int a = 0; a < listaaux.size(); a++) {
			x = listaaux.get(a).x;
			y = listaaux.get(a).y;
			temp = ip.get(x, y);
			vetorcont[temp]++; // contando a quantidade de vezes que cada intensidade aparece, somando a
								// unidade em cada índice do vetor
		}
		int max = -1000;
		for (int a = 0; a < vetorcont.length; a++) {
			if (vetorcont[a] > max) {// nessa condiciona o valor de max só será substituido por um valor mais alto do
										// vetor, de modo que seu valor final é a maior frequência de acontecimento de
										// uma intensidade
				max = vetorcont[a];
				moda = a;// moda recebe o índice do vetor que carrega o valor da maior valor(max).
			}

		}
		intensidademodal.add(moda);
		return intensidademodal;
	}

	private ArrayList<Double> Calcula_Intensidade_Mediana(ArrayList<Point> listaaux, ImageProcessor ip) {// método que
																											// calcula a
																											// intensidade
																											// mediana
																											// do
																											// candidato
																											// a partir
																											// da imagem
																											// original
		double mediana = 0;
		int size = listaaux.size();// variável que recebe a quantidade de pontos

		if ((size % 2) == 0) {// se houver uma quantidade par de pontos (ou seja, o último índice é ímpar)
			int div = size / 2;// variável recebendo a metade do tamanho
			int x1 = listaaux.get(div).x;// pega a coordenada x do ponto da metade
			int y1 = listaaux.get(div).y;// pega a coordenada y do ponto da metade
			int x2 = listaaux.get(div - 1).x;// pega a coordenada x do ponto da metade-1
			int y2 = listaaux.get(div - 1).y;// pega a coordenada y do ponto da metade-1
			int temp1 = ip.get(x1, y1);// variável que recebe a intensidade do ponto da metade
			int temp2 = ip.get(x2, y2);// variável que recebe a intensidade do ponto da metade-1
			mediana = (temp1 + temp2) / 2;// aplicando a definição de mediana quando se tem uma quantidade par de pontos
		} else {
			int div = size / 2;// aplicando a definição de mediana quando se tem uma quantidade ímpar de pontos
			int x1 = listaaux.get(div).x;
			int y1 = listaaux.get(div).y;
			int temp1 = ip.get(x1, y1);
			mediana = temp1;
		}

		intensidademediana.add(mediana);
		return intensidademediana;
	}

	private void Exclui_Area(double area, ArrayList<Point> listaaux, ImageProcessor ip3) {// método que pinta de branco
																							// os pixels que estão
																							// abaixo do limiar definido
																							// para a determinação dos
																							// falsos positivos
		int x;
		int y;
		Color branca = new Color(255, 255, 255);
		int BRANCA = branca.getRGB();
		for (int a = 0; a < listaaux.size(); a++) {
			x = listaaux.get(a).x;
			y = listaaux.get(a).y;
			if (area < 1000) {// aplicando a condição com o limiar escolhido
				ip3.set(x, y, BRANCA);// pinta de branco caso a área do candidato a embrião seja menor que o limiar
			}

		}
	}

	private void Exclui_Borda(double borda, ArrayList<Point> listaaux, ImageProcessor ip3) {// método que pinta de
																							// branco os candidatos a
																							// embriao que se encontram
																							// na borda da imagem
		int x;
		int y;
		Color branca = new Color(255, 255, 255);
		int BRANCA = branca.getRGB();
		for (int a = 0; a < listaaux.size(); a++) {
			x = listaaux.get(a).x;
			y = listaaux.get(a).y;
			if (borda == 1) { // utilisando a lista que identifica candidatos na borda
				ip3.set(x, y, BRANCA);// pintando de branco candidatos na borda
			}
		}
	}

	public void tabelaresumo() {// método que gera uma tabela com as estatísticas de cada embrião

		ResultsTable tabelaresumo = new ResultsTable();// gerando nova tabela

		for (int a = 0; a < area1.size(); a++) {
			tabelaresumo.incrementCounter();
			// gerando os parâmetros que serão utilizados para gerar a tabela
			double area = area1.get(a);
			double centrox = centroide.get(a).x;
			double centroy = centroide.get(a).y;
			double borda1 = borda.get(a);
			double perimetro1 = perimetro.get(a);
			double circularidade1 = circularidade.get(a);
			double media = intensidademedia.get(a);
			double moda = intensidademodal.get(a);
			double mediana = intensidademediana.get(a);
			double min = intensidademinima.get(a);
			double max = intensidademaxima.get(a);

			// adicionando as estatísticas à tabela
			tabelaresumo.addValue("Área", area);
			tabelaresumo.addValue("Centróide", "(" + centrox + "," + centroy + ")");
			tabelaresumo.addValue("Pertence a borda", borda1);
			tabelaresumo.addValue("Perimetro", perimetro1);
			tabelaresumo.addValue("Circularidade", circularidade1);
			tabelaresumo.addValue("Intensidade média:", media);
			tabelaresumo.addValue("Intensidade modal:", moda);
			tabelaresumo.addValue("Intensidade mediana:", mediana);
			tabelaresumo.addValue("Intensidade mínima:", min);
			tabelaresumo.addValue("Intensidade máxima:", max);

		}

		tabelaresumo.show("Tabela de Estatísticas dos Candidatos a Embrião");// mostrando a tabela de estatísticas dos
																				// candidatos a embrião

	}

	@Override
	public int setup(String arg0, ImagePlus imp) {
		// TODO Auto-generated method stub
		this.imp = imp;
		return DOES_ALL;
	}

}
