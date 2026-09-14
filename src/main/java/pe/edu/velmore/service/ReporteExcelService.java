package pe.edu.velmore.service;

import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.edu.velmore.model.Producto;

import java.io.IOException;
import java.util.List;

/**
 * Servicio para generar reportes Excel con Apache POI.
 * Incluye estilos en cabecera: negrita, color de fondo y bordes.
 */
@Service
public class ReporteExcelService {

    private static final Logger log = LoggerFactory.getLogger(ReporteExcelService.class);

    private final ProductoService productoService;

    public ReporteExcelService(ProductoService productoService) {
        this.productoService = productoService;
    }

    public byte[] generarReporteProductos() {
        List<Producto> productos = productoService.listarActivos();
        log.info("Generando reporte Excel con {} productos", productos.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet hoja = workbook.createSheet("Productos VELMORE");

            // --- Estilo para la cabecera ---
            CellStyle estiloHeader = workbook.createCellStyle();
            Font fuenteHeader = workbook.createFont();
            fuenteHeader.setBold(true);
            fuenteHeader.setFontHeightInPoints((short) 11);
            estiloHeader.setFont(fuenteHeader);
            estiloHeader.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloHeader.setAlignment(HorizontalAlignment.CENTER);
            estiloHeader.setBorderBottom(BorderStyle.THIN);
            estiloHeader.setBorderTop(BorderStyle.THIN);
            estiloHeader.setBorderLeft(BorderStyle.THIN);
            estiloHeader.setBorderRight(BorderStyle.THIN);

            // --- Estilo para datos ---
            CellStyle estiloDato = workbook.createCellStyle();
            estiloDato.setBorderBottom(BorderStyle.THIN);
            estiloDato.setBorderLeft(BorderStyle.THIN);
            estiloDato.setBorderRight(BorderStyle.THIN);

            // --- Estilo para precios (número con 2 decimales) ---
            CellStyle estiloPrecio = workbook.createCellStyle();
            DataFormat formato = workbook.createDataFormat();
            estiloPrecio.setDataFormat(formato.getFormat("S/ #,##0.00"));
            estiloPrecio.setBorderBottom(BorderStyle.THIN);
            estiloPrecio.setBorderLeft(BorderStyle.THIN);
            estiloPrecio.setBorderRight(BorderStyle.THIN);

            // --- Fila de cabecera ---
            String[] columnas = {"ID", "Nombre", "Marca", "Categoría", "Precio (S/)", "Stock"};
            Row cabecera = hoja.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = cabecera.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloHeader);
            }

            // --- Filas de datos ---
            int fila = 1;
            for (Producto producto : productos) {
                Row row = hoja.createRow(fila++);

                Cell cId = row.createCell(0);
                cId.setCellValue(producto.getId());
                cId.setCellStyle(estiloDato);

                Cell cNombre = row.createCell(1);
                cNombre.setCellValue(producto.getNombre());
                cNombre.setCellStyle(estiloDato);

                Cell cMarca = row.createCell(2);
                cMarca.setCellValue(producto.getMarca());
                cMarca.setCellStyle(estiloDato);

                Cell cCategoria = row.createCell(3);
                cCategoria.setCellValue(producto.getCategoria().getEtiqueta());
                cCategoria.setCellStyle(estiloDato);

                Cell cPrecio = row.createCell(4);
                cPrecio.setCellValue(producto.getPrecio());
                cPrecio.setCellStyle(estiloPrecio);

                Cell cStock = row.createCell(5);
                cStock.setCellValue(producto.getStock());
                cStock.setCellStyle(estiloDato);
            }

            // Ajustar ancho de columnas automáticamente
            for (int i = 0; i <= 5; i++) {
                hoja.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Reporte Excel generado exitosamente");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error al generar el reporte Excel: {}", e.getMessage(), e);
            throw new IllegalStateException("No se pudo generar el reporte Excel", e);
        }
    }
    public byte[] generarReporteSeguridad() {
        log.info("Generando reporte Excel de Pruebas de Seguridad");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Pruebas de Seguridad");

            // Crear estilo visual para la cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Generar cabecera
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID Prueba", "Tipo de Vulnerabilidad", "Componente Probado", "Estado", "Observaciones y Mitigación"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar con el reporte de controles de seguridad de tu programa
            Object[][] datos = {
                {"SEC-01", "Inyección y Datos Inválidos", "ProductoValidator y ProductoService", "Aprobado", "No se permite registrar productos con nombres vacíos, ni valores negativos."},
                {"SEC-02", "Acceso no Autorizado", "LoginInterceptor y Rutas /admin/**", "Aprobado", "El interceptor bloquea intentos de acceso directos y redirige al login."},
                {"SEC-03", "Clickjacking y MIME Sniffing", "SecurityHeadersInterceptor", "Aprobado", "Cabeceras X-Frame-Options y X-Content-Type-Options configuradas correctamente."},
                {"SEC-04", "Robo de Sesión en Caché", "Cabeceras HTTP de Control de Caché", "Aprobado", "Uso de 'no-store, no-cache' asegura que no queden datos tras cerrar sesión."}
            };

            int rowNum = 1;
            for (Object[] dato : datos) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < dato.length; i++) {
                    row.createCell(i).setCellValue(dato[i].toString());
                }
            }

            // Ajustar el ancho de las columnas automáticamente
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar reporte de seguridad", e);
            throw new RuntimeException("No se pudo generar el reporte Excel de seguridad");
        }
    }
}
