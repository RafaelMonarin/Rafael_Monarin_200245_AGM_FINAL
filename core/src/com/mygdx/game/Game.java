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
	// Variaveis das texturas.
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	// Variaveis das colisoes.
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	// Variaveis para os valores do jogo.
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;
	// Variaveis da interface BitmapFont.
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;
	// Variaveis da interface Sound.
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	// Variaveis da interface preferendes.
	Preferences preferences;
	// Variaveis para camera e tela.
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 1280;
	private final float VIRTUAL_HEIGHT = 720;
	// Primeiro metodo chamdo que cria o aplicativo chamando alguns metodos.
	@Override
	public void create () {
		inicializarTexturas();
		inicializarObjetos();
	}
	// Metodo que e chamado a cada frame (igual o Update() da Unity), e chama alguns metodos para serem atualizados.
	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}
	// Metodo que coloca as texturas nas variaveis criadas.
	private void inicializarTexturas(){
		// Textura de animacao do passaro (jogador).
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");
		// Texturas do cenario.
		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
	}
	// Metodo que inicializa os objetos.
	private void inicializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();
		// Define a largura e a altura do dispositivo, a posicao inicial do passaro no meio da tela, a posicao do cano na direita da tela, e o espaco entre os canos.
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 250;
		// Cria o texto de pontuacao com a cor branca e tamanho 10.
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);
		// Cria o texto de reinicar com a cor verde e tamanho 2.
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);
		// Cria o texto de melhor pontuacao com a cor vermelha e tamanho 2.
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);
		// Cria as colisoes.
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		// Define os sons pegando eles pelos aquivos do projeto.
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		// Define as preferencias e a pontuacao maxima.
		preferences = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferences.getInteger("pontuacaoMaxima", 0);
		// Cria e posiciona a camera e a tela com as dimensoes do aparelho.
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}
	// Metodo que verifica o estado do jogo.
	private void verificarEstadoJogo(){
		// Variavel booleana que detecta o click na tela.
		boolean toqueTela = Gdx.input.justTouched();
		// Se for o estado 0:   (Antes do primeiro Clique).
		if (estadoJogo == 0){
			// O jogo comeca "pausado", só ouvindo se o jogador clicar na tela, e se clicar sobe o passaro (pulo), muda o estado pra 1 e executa o som de pulo.
			if (toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		// Caso o contrario se o estado for 1:   (Apos o primeiro clique).
		} else if (estadoJogo == 1){
			// Se clicar na tela sobe o passaro (pulo) e executa o som de pulo.
			if (toqueTela){
				gravidade = -15;
				somVoando.play();
			}
			// Move o cano para a esquerda.
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 500;
			// Se o cano sair da tela, volta ele para a direita (como se instanciasse outro), define uma altura random e seta a variavel "passouCano" para falso.
			if (posicaoCanoHorizontal < -canoTopo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}
			// Se o passaro estiver no ar ou se clciar na tela, altera a posicao do passaro verticalmente.
			if (posicaoInicialVerticalPassaro > 0 || toqueTela){
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			}
			// Aumenta a gravidade para cair mais rapido.
			gravidade++;
		// Caso o contrario se o estado for 2:   (Quando o passaro morre).
		} else if (estadoJogo == 2){
			// Se os pontos atuais for maior que a pontuacao maxima (recorde), seta a pontuacao maxima com a pontuacao atual e salva no dispositivo.
			if (pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferences.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferences.flush();
			}
			// Move o passaro pra esquerda.
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;
			// Se clicar na tela, reinicia o jogo resetando todas as variaveis.
			if (toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}
	// Metodo que detecta as colisoes.
	private void detectarColisoes(){
		circuloPassaro.set(
				150 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2,
				passaros[0].getWidth() / 2
		);

		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		
		if (colidiuCanoCima || colidiuCanoBaixo){
			if (estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	private void desenharTexturas(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao],
				150 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2,
				alturaDispositivo - 110);

		if (estadoJogo == 2){
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2,
					alturaDispositivo / 2);
			textoReiniciar.draw(batch,
					"Seu record é: " + pontuacaoMaxima + " pontos",
					larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
		}
		batch.end();
	}

	public void validarPontos(){
		if (posicaoCanoHorizontal < 150 - passaros[0].getWidth()){
			if (!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3){
			variacao = 0;
		}
	}

	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}

	@Override
	public void dispose(){

	}
}