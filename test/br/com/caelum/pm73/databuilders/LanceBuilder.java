package br.com.caelum.pm73.databuilders;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.caelum.pm73.dominio.Lance;
import br.com.caelum.pm73.dominio.Usuario;

public class LanceBuilder {
	private List<Lance> lances = new ArrayList<Lance>();
	private double valorAtual;
	private Calendar data = Calendar.getInstance();
	private Usuario usuario = new Usuario("Joao da Silva", "joao@silva.com.br");
	private LeilaoBuilder leilaoBuilder;
	
	public LanceBuilder(LeilaoBuilder leilaoBuilder) {
		super();
		this.leilaoBuilder = leilaoBuilder;
	}

	public LanceBuilder naData( Calendar data ){
		this.data = data;
		
		return this;
	}
	
    public LanceBuilder novoLancePara( Usuario usuario ){
    	this.usuario = usuario;
    	
    	return this;
    }
	
	public LanceBuilder noValorDe( double novoValor ){
		this.valorAtual = novoValor;
		
		criarLanceEAdicionarNaResposta();
		
		return this;
	}
	
	private void criarLanceEAdicionarNaResposta(){
		lances.add( new Lance(data, usuario, valorAtual, leilaoBuilder.getLeilao() ) );
	}
	
	public LeilaoBuilder encerrarLances(){
		return leilaoBuilder;
	}
	
	List<Lance> construir(){
		return lances;
	}
}
