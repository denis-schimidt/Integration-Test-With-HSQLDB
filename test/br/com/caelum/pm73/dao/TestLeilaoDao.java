package br.com.caelum.pm73.dao;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hamcrest.Matcher;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.caelum.pm73.databuilders.LeilaoBuilder;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class TestLeilaoDao {
	private static Usuario ciclano;
	private static Usuario fulano;
	private static Usuario beltrano;
	
	private Session session;
	
	private LeilaoDao leilaoDao;

	@BeforeClass
	public static void init() {
		ciclano = new Usuario("Ciclano da Silva", "ciclano.silva@gmail.com");
		fulano = new Usuario("Fulano da Silva", "fulano.silva@gmail.com");
		beltrano = new Usuario("Beltrano da Silva", "beltrano.silva@gmail.com");
	}

	@Before
	public void abrirTransacao() {
		session = new CriadorDeSessao().getSession();
		leilaoDao = new LeilaoDao(session);
		session.beginTransaction();
		
		session.save(ciclano);
		session.save(fulano);
		session.save(beltrano);
	}

	@After
	public void rollbackNaTransacao() {
		session.getTransaction().rollback();
		session.close();
	}

	@Test
	public void deveRetornarLeiloesDentroDoIntervalo() {
		Leilao leilao1 = new LeilaoBuilder().comDono(ciclano).diasAtras(2).construir();
		Leilao leilao2 = new LeilaoBuilder().comDono(ciclano).diasAtras(10).construir();
		Leilao leilao3 = new LeilaoBuilder().comDono(ciclano).diasAtras(8).construir();

		session.save(leilao1);
		session.save(leilao2);
		session.save(leilao3);

		LeilaoDao leilaoDao = new LeilaoDao(session);

		Calendar dataInicialQuery = criarDataDiasAtras(9);
		Calendar dataFinalQuery = Calendar.getInstance();

		List<Leilao> leiloes = leilaoDao.porPeriodo(dataInicialQuery,
				dataFinalQuery);

		assertEquals(leiloes.size(), 2);

		assertThat(leiloes.get(0).isEncerrado(), equalTo(false));
		assertThat(leiloes.get(0).getDataAbertura(),
				between(dataInicialQuery, dataFinalQuery));
	}

	@Test
	public void naoDeveRetornarLeiloesDentroDoIntervaloEEncerrados() {
		Leilao leilao1 = new LeilaoBuilder().comDono(ciclano).diasAtras(2).construir();
		Leilao leilao2 = new LeilaoBuilder().comDono(ciclano).diasAtras(10).construir();
		Leilao leilao3 = new LeilaoBuilder().comDono(ciclano).diasAtras(8).encerrado()
				.construir();

		session.save(leilao1);
		session.save(leilao2);
		session.save(leilao3);

		LeilaoDao leilaoDao = new LeilaoDao(session);

		Calendar dataInicialQuery = criarDataDiasAtras(9);
		Calendar dataFinalQuery = Calendar.getInstance();

		List<Leilao> leiloes = leilaoDao.porPeriodo(dataInicialQuery,
				dataFinalQuery);

		assertEquals(leiloes.size(), 1);

		assertThat(leiloes.get(0).isEncerrado(), equalTo(false));
		assertThat(leiloes.get(0).getDataAbertura(),
				between(dataInicialQuery, dataFinalQuery));
	}
	
	@Test
	public void deveRetornarLeiloesDisputados(){

		for (Leilao leilao : criarMassaDadosParaLeiloesDisputados() ) {
			session.save(leilao);	
		}
		
		List<Leilao> leiloes = leilaoDao.disputadosEntre( 330.0, 1000.0 );
		
		assertThat( leiloes.size(), equalTo( 1 ) );
		assertThat( leiloes.get( 0 ).getNome(), equalToIgnoringCase( "Computador X" ) );
	}
	
	private List<Leilao> criarMassaDadosParaLeiloesDisputados(){
		List<Leilao> leiloes = new ArrayList<Leilao>();
		
		//Leilao retorna da Query 
		leiloes.add( new LeilaoBuilder().comDono(ciclano).comNome("Computador X").comValorInicial( 500.0 )
				.novoLancePara(ciclano).noValorDe( 500.0 )
				.novoLancePara(fulano).noValorDe( 600.0 )
				.novoLancePara(ciclano).noValorDe( 700.0 )
				.novoLancePara(fulano).noValorDe( 800.0 )
				.encerrarLances()
				.construir() );
		
		//Leilao não retorna da Query ( Regra: > 3 lances )
		leiloes.add( new LeilaoBuilder().comDono(ciclano).comValorInicial( 500.0 )
				.novoLancePara(ciclano).noValorDe( 500.0 )
				.novoLancePara(fulano).noValorDe( 600.0 )
				.novoLancePara(ciclano).noValorDe( 700.0 )
				.encerrarLances()
				.construir() );
		
		//Leilao não retorna da Query ( Regra: encerrado = false )
		leiloes.add( new LeilaoBuilder().comDono(ciclano).encerrado().comValorInicial( 500.0 )
				.novoLancePara(ciclano).noValorDe( 500.0 )
				.novoLancePara(fulano).noValorDe( 600.0 )
				.novoLancePara(ciclano).noValorDe( 700.0 )
				.encerrarLances()
				.construir() );

		//Leilao não retorna da Query ( Regra: faixa de valores para valor inicial do leilao  ) 
		leiloes.add( new LeilaoBuilder().comDono(ciclano).comValorInicial( 329.99 )
				.novoLancePara(ciclano).noValorDe( 500.0 )
				.novoLancePara(fulano).noValorDe( 600.0 )
				.novoLancePara(ciclano).noValorDe( 700.0 )
				.encerrarLances()
				.construir() );
		
		//Leilao não retorna da Query ( Regra: faixa de valores para valor inicial do leilao  ) 
		leiloes.add( new LeilaoBuilder().comDono(ciclano).comValorInicial( 1000.01 )
				.novoLancePara(ciclano).noValorDe( 1001.0 )
				.novoLancePara(fulano).noValorDe( 1100.0 )
				.novoLancePara(ciclano).noValorDe( 1220.0 )
				.novoLancePara(ciclano).noValorDe( 1320.0 )
				.encerrarLances()
				.construir() );
		
		return leiloes;
	}
	
	@Test
	public void deveRetornarApenasLeiloesEmQueUsuarioDeuPeloMenosUmLance(){
		Leilao leilao1 = new LeilaoBuilder().comDono(ciclano).comNome( "Smartphone Android")
			.novoLancePara(ciclano).noValorDe( 1200 )
			.novoLancePara(ciclano).noValorDe( 1300 )
			.encerrarLances()
			.construir();
		
		Leilao leilao2 = new LeilaoBuilder().comDono(ciclano).comNome( "TV UltraHD")
			.novoLancePara(ciclano).noValorDe( 1200 )
			.novoLancePara(fulano).noValorDe( 1300 )
			.encerrarLances()
			.construir();
		
		session.save(leilao1);
		session.save(leilao2);
		
		List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario( fulano );
		
		assertThat( leiloes.size(), equalTo( 1 ) );
		assertThat( leiloes.get( 0 ).getNome(), equalTo( "TV UltraHD" ) );
	}
	
	//@Test
	public void deveRetornarLeiloesDoUsuarioSemRepeticao(){
		Leilao leilao1 = new LeilaoBuilder().comDono(ciclano).comNome( "Smartphone Android")
			.novoLancePara(ciclano).noValorDe( 1200 )
			.novoLancePara(fulano).noValorDe( 1230 )
			.novoLancePara(ciclano).noValorDe( 1300 )
			.encerrarLances()
			.construir();
		
		Leilao leilao2 = new LeilaoBuilder().comDono(ciclano).comNome( "TV UltraHD")
			.novoLancePara(fulano).noValorDe( 1200 )
			.novoLancePara(beltrano).noValorDe( 1300 )
			.encerrarLances()
			.construir();
		
		session.save(leilao1);
		session.save(leilao2);
		
		List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario( ciclano );
		
		assertThat( leiloes.size(), equalTo( 1 ) );
		assertThat( leiloes.get( 0 ).getNome(), equalTo( "Smartphone Android" ) );
	}
	
	//@Test
	public void deveRetornarValorInicialMedioDeUmUsuario(){
		Leilao leilao1 = new LeilaoBuilder().comDono(ciclano)
				.comValorInicial( 1000 )
				.novoLancePara(ciclano).noValorDe( 1200 )
				.novoLancePara(fulano).noValorDe( 1230 )
				.novoLancePara(ciclano).noValorDe( 1300 )
				.encerrarLances()
				.construir();
		
		Leilao leilao2 = new LeilaoBuilder().comDono(beltrano)
				.comValorInicial( 400 )
				.novoLancePara(ciclano).noValorDe( 500 )
				.novoLancePara(fulano).noValorDe( 600 )
				.novoLancePara(beltrano).noValorDe( 700 )
				.encerrarLances()
				.construir();
		
		session.save(leilao1);
		session.save(leilao2);
		
		double valorInicialMedioDoUsuario = leilaoDao.getValorInicialMedioDoUsuario(ciclano);
		
		assertThat(valorInicialMedioDoUsuario, closeTo( 700.0, 0.0001 ) );
	}
	
	@Test
	public void deveExcluirLeilao(){
		Leilao leilao1 = new LeilaoBuilder().comDono(ciclano)
				.comValorInicial( 1000 )
				.novoLancePara(ciclano).noValorDe( 1200 )
				.novoLancePara(fulano).noValorDe( 1230 )
				.novoLancePara(ciclano).noValorDe( 1300 )
				.encerrarLances()
				.construir();
		
		session.save(leilao1);
		session.delete(leilao1);
		
		session.flush();
		session.clear();
		
		assertNull( leilaoDao.porId( leilao1.getId() ) );
	}
	
	@SuppressWarnings("unchecked")
	private Matcher<Calendar> between(Calendar dataInicialQuery,
			Calendar dataFinalQuery) {
		return allOf(greaterThanOrEqualTo(dataInicialQuery),
				lessThanOrEqualTo(dataFinalQuery));
	}

	private Calendar criarDataDiasAtras(int diasAtras) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -diasAtras);

		return calendar;
	}
}
