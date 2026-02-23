package br.com.talentcore.talentos.domain;

import java.time.LocalDateTime;

public class Anexo {
    private String id;
    private String candidatoId;
    private TipoAnexo tipo;
    private String urlArquivo;
    private LocalDateTime dataUpload;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCandidatoId() { return candidatoId; }
    public void setCandidatoId(String candidatoId) { this.candidatoId = candidatoId; }
    public TipoAnexo getTipo() { return tipo; }
    public void setTipo(TipoAnexo tipo) { this.tipo = tipo; }
    public String getUrlArquivo() { return urlArquivo; }
    public void setUrlArquivo(String urlArquivo) { this.urlArquivo = urlArquivo; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}
