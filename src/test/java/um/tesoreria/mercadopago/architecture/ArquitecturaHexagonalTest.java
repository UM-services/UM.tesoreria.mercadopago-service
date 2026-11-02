package um.tesoreria.mercadopago.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la regla de dependencias de la arquitectura hexagonal sobre las fuentes:
 * dominio puro, aplicacion sin infraestructura, slices de datos/gateways aisladas,
 * shared como kernel y procesos como unico punto de composicion.
 */
class ArquitecturaHexagonalTest {

    private static final Path BASE = Path.of("src/main/java/um/tesoreria/mercadopago/hexagonal/mercadopago");
    private static final String ROOT = "um.tesoreria.mercadopago.hexagonal.mercadopago.";

    @Test
    void elDominioSoloDependeDeJavaYLombok() throws IOException {
        for (Path file : filesUnder("domain")) {
            String slice = sliceOf(file);
            boolean esModelo = file.toString().replace('\\', '/').contains("/domain/model/");
            String propio = ROOT + slice.replace('/', '.') + ".domain.model.";
            for (String dependency : imports(file)) {
                boolean permitido = dependency.matches("^(java|javax)\\..*|^lombok\\..*");
                if (!permitido && !esModelo && dependency.startsWith(ROOT + "shared.domain.model.")) {
                    permitido = true;
                }
                if (!permitido && !esModelo && dependency.startsWith(propio)) {
                    permitido = true;
                }
                assertThat(permitido)
                        .as("%s solo puede importar java.*, lombok.*%s: %s",
                                file, esModelo ? "" : " y el modelo de su propia slice", dependency)
                        .isTrue();
            }
        }
    }

    @Test
    void laAplicacionNoDependeDeInfraestructuraNiDeFeignNiDeJackson() throws IOException {
        for (Path file : filesUnder("application")) {
            for (String dependency : imports(file)) {
                assertThat(dependency)
                        .as("%s no puede depender de infraestructura", file)
                        .doesNotContain(".infrastructure.")
                        .doesNotStartWith("feign.")
                        .doesNotStartWith("com.fasterxml.")
                        .doesNotStartWith("com.mercadopago.")
                        .doesNotStartWith("jakarta.")
                        .doesNotStartWith("org.springframework.web.")
                        .doesNotStartWith("org.springframework.http.")
                        .doesNotStartWith("org.springframework.cloud.");
            }
        }
    }

    @Test
    void laAplicacionSoloUsaAnotacionesDeSpringPermitidas() throws IOException {
        for (Path file : filesUnder("application")) {
            for (String dependency : imports(file)) {
                if (dependency.startsWith("org.springframework.")) {
                    assertThat(dependency)
                            .as("%s solo puede usar stereotype, transaction o @Value", file)
                            .matches("^org\\.springframework\\.(stereotype|transaction\\.annotation|beans\\.factory\\.annotation)\\..*");
                }
            }
        }
    }

    @Test
    void sharedNoDependeDeNingunaSlice() throws IOException {
        for (Path file : allFiles()) {
            if (!"shared".equals(sliceOf(file))) {
                continue;
            }
            for (String dependency : projectImports(file)) {
                assertThat(dependency)
                        .as("%s (shared) no puede depender de una slice", file)
                        .doesNotStartWith(ROOT + "datos.")
                        .doesNotStartWith(ROOT + "gateways.")
                        .doesNotStartWith(ROOT + "procesos.");
            }
        }
    }

    @Test
    void lasSlicesDeDatosYGatewaysNoImportanOtrasSlices() throws IOException {
        for (Path file : allFiles()) {
            String slice = sliceOf(file);
            if (!slice.startsWith("datos/") && !slice.startsWith("gateways/")) {
                continue;
            }
            for (String dependency : projectImports(file)) {
                if (dependency.startsWith(ROOT + "shared.")) {
                    continue;
                }
                assertThat(dependency)
                        .as("%s (slice %s) no puede importar otra slice", file, slice)
                        .startsWith(ROOT + slice.replace('/', '.') + ".");
            }
        }
    }

    @Test
    void losProcesosNoImportanInfraestructuraAjena() throws IOException {
        for (Path file : allFiles()) {
            String slice = sliceOf(file);
            if (!slice.startsWith("procesos/")) {
                continue;
            }
            for (String dependency : projectImports(file)) {
                if (dependency.startsWith(ROOT + "shared.")) {
                    continue;
                }
                String importedSlice = importedSlice(dependency);
                if (importedSlice != null && importedSlice.startsWith("procesos/")
                        && !importedSlice.equals(slice)) {
                    continue;
                }
                assertThat(dependency)
                        .as("%s no puede importar infraestructura de otra slice", file)
                        .doesNotContain(".infrastructure.");
            }
        }
    }

    @Test
    void losControladoresNoDependenDePersistencia() throws IOException {
        for (Path file : filesUnder("infrastructure/web/controller")) {
            String source = Files.readString(file);
            assertThat(source)
                    .as("%s no puede depender de persistencia ni entidades JPA", file)
                    .doesNotContain(".persistence.")
                    .doesNotContain("Jpa");
        }
    }

    @Test
    void losPaquetesLegadosYaNoExisten() {
        Path root = Path.of("src/main/java/um/tesoreria/mercadopago");
        assertThat(root.resolve("controller")).doesNotExist();
        assertThat(root.resolve("service")).doesNotExist();
        assertThat(root.resolve("client")).doesNotExist();
        assertThat(root.resolve("domain")).doesNotExist();
        assertThat(root.resolve("producer")).doesNotExist();
        assertThat(root.resolve("scheduled")).doesNotExist();
        assertThat(root.resolve("util")).doesNotExist();
        assertThat(root.resolve("hexagonal/preference")).doesNotExist();
    }

    private List<Path> allFiles() throws IOException {
        try (Stream<Path> files = Files.walk(BASE)) {
            return files.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private List<Path> filesUnder(String fragment) throws IOException {
        return allFiles().stream()
                .filter(path -> path.toString().replace('\\', '/').contains("/" + fragment + "/"))
                .toList();
    }

    private List<String> imports(Path file) throws IOException {
        return Files.readAllLines(file).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("import "))
                .map(line -> line.substring("import ".length(), line.length() - 1))
                .filter(line -> !line.startsWith("static "))
                .toList();
    }

    private List<String> projectImports(Path file) throws IOException {
        return imports(file).stream()
                .filter(dependency -> dependency.startsWith(ROOT))
                .toList();
    }

    private String sliceOf(Path file) {
        String relative = BASE.relativize(file).toString().replace('\\', '/');
        String[] parts = relative.split("/");
        if ("shared".equals(parts[0])) {
            return "shared";
        }
        return parts[0] + "/" + parts[1];
    }

    private String importedSlice(String dependency) {
        String relative = dependency.substring(ROOT.length());
        String[] parts = relative.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        if ("shared".equals(parts[0])) {
            return "shared";
        }
        return parts[0] + "/" + parts[1];
    }

}
