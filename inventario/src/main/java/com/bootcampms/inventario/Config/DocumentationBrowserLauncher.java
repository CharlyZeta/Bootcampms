package com.bootcampms.inventario.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@Profile("dev") // Solo se activa en el perfil de desarrollo
public class DocumentationBrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(DocumentationBrowserLauncher.class);

    @Value("https://charlyzeta.github.io/Bootcampms/")
    private String documentationUrl;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (documentationUrl != null && !documentationUrl.isEmpty()) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(documentationUrl));
                    log.info("Intentando abrir la documentación en el navegador: {}", documentationUrl);
                } catch (IOException | URISyntaxException e) {
                    log.error("No se pudo abrir la URL de documentación '{}' en el navegador: {}", documentationUrl, e.getMessage());
                } catch (UnsupportedOperationException e) {
                    log.warn("La operación de abrir el navegador no es soportada en este entorno para la URL: {}", documentationUrl);
                }
            } else {
                log.warn("El entorno de escritorio o la acción BROWSE no son soportados. No se abrirá la URL: {}", documentationUrl);
                log.info("Puedes acceder a la documentación manualmente en: {}", documentationUrl);
            }
        } else {
            log.info("No se ha configurado 'documentation.auto-open-url' o está vacía. No se abrirá ninguna URL automáticamente.");
        }
    }
}