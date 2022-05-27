package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.w3c.dom.Text;

import java.util.Random;

public class Game extends ApplicationAdapter {
	// Variáveis das texturas.
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture[] coin;
	// Variáveis das colisões.
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Circle circuloCoin;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	// Variáveis para os valores do jogo.
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private float posicaoCoinHorizontal;
	private float posicaoCoinVertical;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private boolean coletouCoin = false;
	private boolean colidiuCoin = false;
	private int coinType;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;
	// Variáveis da interface BitmapFont.
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;
	// Variáveis da interface Sound.
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	Sound somCoin;
	// Variáveis da interface Preferences.
	Preferences preferences;
	// Variáveis para câmera e tela.
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;
	// Primeiro método chamdo que cria o aplicativo chamando alguns métodos.
	@Override
	public void create () {
		inicializarTexturas();
		inicializarObjetos();
	}
	// Método que e chamado a cada frame (igual o Update() da Unity), e chama alguns métodos para serem atualizados.
	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}
	// Método que coloca as texturas nas variáveis criadas.
	private void inicializarTexturas(){
		// Textura de animação do pássaro (jogador).
		passaros = new Texture[3];
		passaros[0] = new Texture("AngryBird1.png");
		passaros[1] = new Texture("AngryBird2.png");
		passaros[2] = new Texture("AngryBird3.png");
		// Texturas do cenário.
		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		// Textura das moedas;
		coin = new Texture[2];
		coin[0] = new Texture("SilverCoin.png");
		coin[1] = new Texture("GoldCoin.png");
	}
	// Meétodo que inicializa os objetos.
	private void inicializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();
		// Define a largura e a altura do dispositivo, a posição inicial do pássaro no meio da tela, a posição do cano na direita da tela, e o espaço entre os canos.
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		posicaoCoinVertical = alturaDispositivo / 2;
		posicaoCoinHorizontal = larguraDispositivo;
		espacoEntreCanos = 250;
		// Cria o texto de pontução com a cor branca e tamanho 10.
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);
		// Cria o texto de reinicar com a cor verde e tamanho 2.
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);
		// Cria o texto de melhor pontuação com a cor vermelha e tamanho 2.
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);
		// Cria as colisões.
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		circuloCoin = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		// Define os sons pegando eles pelos arquivos do projeto.
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		somCoin = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));
		// Define as preferências e a pontuação máxima.
		preferences = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferences.getInteger("pontuacaoMaxima", 0);
		// Cria e posiciona a câmera e a tela com as dimensões do aparelho.
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}
	// Método que verifica o estado do jogo.
	private void verificarEstadoJogo(){
		// Variável booleana que detecta o click na tela.
		boolean toqueTela = Gdx.input.justTouched();
		// Se o estado for 0:   (Antes do primeiro Clique).
		if (estadoJogo == 0){
			// O jogo começa "pausado", só ouvindo se o jogador clicar na tela, e se clicar sobe o pássaro (pulo), muda o estado para 1 e executa o som de pulo.
			if (toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		// Caso o contrário se o estado for 1:   (Após o primeiro clique).
		} else if (estadoJogo == 1){
			// Se clicar na tela sobe o pássaro (pulo) e executa o som de pulo.
			if (toqueTela){
				gravidade = -15;
				somVoando.play();
			}
			// Move o cano para a esquerda.
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 500;
			posicaoCoinHorizontal -= Gdx.graphics.getDeltaTime() * 500;
			// Se o cano sair da tela, volta ele para a direita (como se instanciasse outro), define uma altura random e seta a váriavel "passouCano" para falso.
			if (posicaoCanoHorizontal < -canoTopo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt( 400) - 200;
				passouCano = false;
			}
			if (posicaoCoinHorizontal < -coin[0].getWidth()){
				posicaoCoinHorizontal = larguraDispositivo + random.nextInt(500);
				posicaoCoinVertical = random.nextInt((int)alturaDispositivo);
				coletouCoin = false;
				coinType = random.nextInt(2);
			}
			// Se o pássaro estiver no ar ou se clicar na tela, altera a posição do passaro verticalmente.
			if (posicaoInicialVerticalPassaro > 0 || toqueTela){
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			}
			// Aumenta a gravidade para o pássaro cair.
			gravidade++;
		// Caso o contrário se o estado for 2:   (Quando o pássaro morre).
		} else if (estadoJogo == 2){
			// Se os pontos for maior que a pontuação máxima (record), seta a pontuação máxima com a pontuação atual e salva no dispositivo.
			if (pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferences.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferences.flush();
			}
			// Move o passaro para esquerda.
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;
			// Se clicar na tela, reinicia o jogo resetando todas as variáveis.
			if (toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCoinHorizontal = larguraDispositivo;
			}
		}
	}
	// Método que detecta as colisões.
	private void detectarColisoes(){
		// Seta a posição X e Y e o raio da colisão de circulo.
		circuloPassaro.set(
				150 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2,
				passaros[0].getWidth() / 2
		);
		// Seta a posição X e Y, a altura e largura da colisão de retângulo no cano de baixo.
		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);
		// Seta a posição X e Y, a altura e largura da colisão de retângulo no cano de cima.
		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		// Seta a posição X e Y e o raio da colisão de circulo.

		circuloCoin.set(posicaoCoinHorizontal, posicaoCoinVertical, coin[0].getWidth() / 2);
		// Cria um variável booleana que detecta a colisão entre os canos e o pássaro.
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		colidiuCoin = Intersector.overlaps(circuloPassaro, circuloCoin);
		// Se colidir com algum cano (de cima ou de baixo):
		if (colidiuCanoCima || colidiuCanoBaixo){
			// Se o estado for 1, executa o som de colisão e muda o estado do jogo para 2.
			if (estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}
	// Mátodo que desenha as texturas.
	private void desenharTexturas(){
		// Seta a matrix que sera usada.
		batch.setProjectionMatrix(camera.combined);
		// Seta o começo da batch para os desenhos.
		batch.begin();
		// Desenha a textura do fundo (cenário), na posição X e Y, e define sua largura e altura.
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		// Desenha a textura do pássaro na posição X e Y.
		batch.draw(passaros[(int) variacao],
				150 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		// Desenha o cano de baixo, na posição X e Y.
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		// Desenha o cano de cima, na posição X e Y.
		batch.draw(canoTopo, posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		// Desenha a moeda na posição X e Y.
		batch.draw(coin[coinType], posicaoCoinHorizontal, posicaoCoinVertical);
		// Desenha o texto da pontuação na posição X e Y.
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2 - 50,
				alturaDispositivo - 110);
		// Se o estado for 2:
		if (estadoJogo == 2){
			// Desenha o texto de game over na posição X e Y.
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2,
					alturaDispositivo / 2);
			// Desenha o texto de reinicar na posição X e Y, mostrando seu record.
			textoReiniciar.draw(batch,
					"Seu record é: " + pontuacaoMaxima + " pontos",
					larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
		}
		// Seta o fim da batch para os desenhos.
		batch.end();
	}
	// Método que valida os pontos.
	public void validarPontos(){
		// Se o cano passar pelo pássaro:
		if (posicaoCanoHorizontal < 150 - passaros[0].getWidth()){
			// Se a variával "passouCano" for falsa, soma 1 ponto, seta a variável verdadeira e executa o som de pontuação.
			// (A variável serve para executar apenaz uma vez essa condição quando o cano passar pelo pássaro, até que a variável seja resetada pelo estado 1).
			if (!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
		// Se colidiu com a moeda e nao coletou ela:
		if(colidiuCoin && !coletouCoin){
			// Dependendo do tipo da moeda, adiciona uma quantidade de pontos diferentes e toca um som.
			coletouCoin = true;
			colidiuCoin = false;
			somCoin.play();
			switch (coinType){
				case 0:
					pontos += 5;
					break;
				case 1:
					pontos += 10;
					break;
			}
		}
		// Velocidade que vai trocar as texturas do pássaro para a animação.
		variacao += Gdx.graphics.getDeltaTime() * 10;
		// Se a variavel "variacao" for maior que 3, volta o valor para 0.
		// (0 = primeira textura da animação, 3 = ultima textura da animação).
		if (variacao > 3){
			variacao = 0;
		}
	}
	// Método para fazer o resize da tela.
	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}
	// Método chamado quando o aplicativo é destruído.
	@Override
	public void dispose(){

	}
}