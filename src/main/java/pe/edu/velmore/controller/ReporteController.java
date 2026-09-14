package pe.edu.velmore.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.velmore.service.ReporteExcelService;

@Controller
public class ReporteController {
    private final ReporteExcelService reporteExcelService;

    public ReporteController(ReporteExcelService reporteExcelService) {
        this.reporteExcelService = reporteExcelService;
    }

    @GetMapping("/reporte/productos")
    public ResponseEntity<byte[]> descargarProductos() {
        byte[] archivo = reporteExcelService.generarReporteProductos();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos_velmore.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo);
    }
    @GetMapping("/reporte/seguridad")
    public ResponseEntity<byte[]> descargarReporteSeguridad() {
        byte[] archivo = reporteExcelService.generarReporteSeguridad();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_pruebas_seguridad.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo);
    }
}
