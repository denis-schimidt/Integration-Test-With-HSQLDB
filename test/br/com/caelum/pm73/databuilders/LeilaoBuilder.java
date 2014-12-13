package br.com.caelum.pm73.databuilders;

import java.util.Calendar;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoBuilder {
    private Usuario dono;
    private double valor;
    private String nome;
    private boolean usado;
    private Calendar dataAbertura;
    private boolean encerrado;
    private Leilao leilao = new Leilao();
    
    private LanceBuilder lanceBuilder = new LanceBuilder( this );

    public LeilaoBuilder() {
        this.dono = new Usuario("Joao da Silva", "joao@silva.com.br");
        this.valor = 100.0;
        this.nome = "XBox";
        this.usado = false;
        this.encerrado = false;
        this.dataAbertura = Calendar.getInstance();
    }
    
    public LeilaoBuilder comDono(Usuario dono) {
        this.dono = dono;
        
        return this;
    }

    public LeilaoBuilder comValorInicial(double valor) {
        this.valor = valor;
        return this;
    }
    
    Leilao getLeilao() {
		return leilao;
	}

    public LeilaoBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public LeilaoBuilder usado() {
        this.usado = true;
        return this;
    }

    public LeilaoBuilder encerrado() {
        this.encerrado = true;
        return this;
    }

    public LeilaoBuilder diasAtras(int dias) {
        Calendar data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_MONTH, -dias);

        this.dataAbertura = data;

        return this;
    }
    
    public LanceBuilder novoLancePara( Usuario usuario ){
    	lanceBuilder.novoLancePara(usuario);
    	
    	return lanceBuilder;
    }

    public Leilao construir() {
        leilao.setNome(nome);
        leilao.setDataAbertura(dataAbertura);
        leilao.setDono(dono);
        leilao.setValorInicial( valor );
        leilao.setUsado(usado);
        leilao.setLances( lanceBuilder.construir() );
        
        if(encerrado) leilao.encerra();
        
        return leilao;
    }
}
