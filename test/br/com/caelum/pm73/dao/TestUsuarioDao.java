package br.com.caelum.pm73.dao;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Usuario;

public class TestUsuarioDao {

	private Session session;
	private UsuarioDao usuarioDao;
	
	@Before
	public void iniciarSessao(){
		session = new CriadorDeSessao().getSession();
		session.beginTransaction();
		usuarioDao = new UsuarioDao(session);
	}
	
	@After
	public void encerrarSessao(){
		session.getTransaction().rollback();
		session.close();
	}
	
	@Test
	public void deveExcluirUsuario(){
		Usuario fulano = new Usuario("Fulano", "fulano.silva@gmail.com" );
		
		usuarioDao.salvar( fulano );
		usuarioDao.deletar( fulano );
		
		session.flush();
		session.clear();
		
		Usuario usuarioRecuperado = usuarioDao.porNomeEEmail( fulano.getNome(), fulano.getEmail() );
		
		assertThat(usuarioRecuperado, nullValue() );
	}
	
	@Test
	public void alterarUsuario(){
		Usuario fulano = new Usuario("Fulano", "fulano@gmail.com" );
		
		usuarioDao.salvar( fulano );
		fulano.setNome( "Fulano Silva");
		fulano.setEmail( "fulano.silva@gmail.com" );
		
		session.flush();
		
		Usuario usuarioInexistente = usuarioDao.porNomeEEmail( "Fulano", "fulano@gmail.com" );
		Usuario usuarioExistente = usuarioDao.porNomeEEmail( "Fulano Silva", "fulano.silva@gmail.com" );
		
		assertNull( usuarioInexistente );
		assertNotNull( usuarioExistente );
	}
}
