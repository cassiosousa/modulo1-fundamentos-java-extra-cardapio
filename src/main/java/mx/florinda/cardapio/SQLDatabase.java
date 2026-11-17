package mx.florinda.cardapio;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLDatabase implements Database {

    @Override
    public List<ItemCardapio> listaItensCardapio() {
        String sql = "SELECT id, nome, descricao, categoria, preco, preco_promocional FROM item_cardapio";
        return executeQuery(sql);
    }

    @Override
    public int totalItensCardapio() {
        String sql = "SELECT count(*) FROM item_cardapio";
        return countQuery(sql);
    }

    @Override
    public void adicionaItemCardapio(ItemCardapio item) {
        String sql = "INSERT INTO item_cardapio (id, nome, descricao, categoria, preco, preco_promocional) VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setLong(1, item.id());
            ps.setString(2, item.nome());
            ps.setString(3, item.descricao());
            ps.setString(4, item.categoria().name());
            ps.setBigDecimal(5, item.preco());
            ps.setBigDecimal(6, item.precoPromocional());

            ps.execute();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ItemCardapio> itemCardapioPorId(Long id) {
        String sql = "SELECT * FROM item_cardapio where id=?";
        return executeQuery(sql, Map.of(1, id)).stream().findFirst();
    }

    @Override
    public boolean removeItemCardapio(Long id) {
        String sql = "delete from item_cardapio where id =?";

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setLong(1, id);
            return ps.execute();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean alteraPrecoItemCardapio(Long id, BigDecimal novoPreco) {
        throw new UnsupportedOperationException("TODO");
    }

    private int countQuery(String sql) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ItemCardapio> executeQuery(String sql) {
        return executeQuery(sql, Map.of());
    }

    private List<ItemCardapio> executeQuery(String sql, Map<Integer, Object> params) {

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {

            try (ResultSet rs = ps.executeQuery()) {
                for (Map.Entry<Integer, Object> entry : params.entrySet()) {
                    Integer key = entry.getKey();
                    Object value = entry.getValue();
                    ps.setInt(key, Integer.parseInt(value.toString()));
                }
                List<ItemCardapio> itensCardapio = new ArrayList<>();
                while (rs.next()) {
                    ItemCardapio itemCardapio = readLine(rs);
                    itensCardapio.add(itemCardapio);
                }
                return itensCardapio;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ItemCardapio readLine(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nome = rs.getString("nome");
        String descricao = rs.getString("descricao");
        String categoriaStr = rs.getString("categoria");
        BigDecimal preco = rs.getBigDecimal("preco");
        BigDecimal precoPromocional = rs.getBigDecimal("preco_promocional");

        ItemCardapio.CategoriaCardapio categoria = ItemCardapio.CategoriaCardapio.valueOf(categoriaStr);

        return new ItemCardapio(id, nome, descricao, categoria, preco, precoPromocional);

    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/cardapio", "root", "senha123");
    }


}
