package gilianmarques.dev.musicplayer.persistence.native_database;

/**
 * Criado por Gilian Marques
 * Terça-feira, 23 de Abril de 2019  as 20:08:53.
 */
class SQL {
    public static String scape(String name) {
        return name.replace("'","\'\'");
    }
}
