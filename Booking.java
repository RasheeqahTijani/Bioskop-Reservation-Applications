package GUI;

	public class Booking {
	    private int idReservasi;
	    private String nama;
	    private String film;
	    private String studio;
	    private String kursi;
	    private String tanggal;
	    private String jam;
	
	    public Booking(int idReservasi, String nama, String film, String studio, String kursi, String tanggal, String jam) {
	        this.idReservasi = idReservasi;
	        this.nama = nama;
	        this.film = film;
	        this.studio = studio;
	        this.kursi = kursi;
	        this.tanggal = tanggal;
	        this.jam = jam;
	    }
	
		public int getIdReservasi() {
			return idReservasi;
		}
	
		public void setIdReservasi(int idReservasi) {
			this.idReservasi = idReservasi;
		}
	
		public String getNama() {
			return nama;
		}
	
		public void setNama(String nama) {
			this.nama = nama;
		}
	
		public String getFilm() {
			return film;
		}
	
		public void setFilm(String film) {
			this.film = film;
		}
	
		public String getStudio() {
			return studio;
		}
	
		public void setStudio(String studio) {
			this.studio = studio;
		}
	
		public String getKursi() {
			return kursi;
		}
	
		public void setKursi(String kursi) {
			this.kursi = kursi;
		}
	
		public String getTanggal() {
			return tanggal;
		}
	
		public void setTanggal(String tanggal) {
			this.tanggal = tanggal;
		}
	
		public String getJam() {
			return jam;
		}
	
		public void setJam(String jam) {
			this.jam = jam;
		}
	
	}
