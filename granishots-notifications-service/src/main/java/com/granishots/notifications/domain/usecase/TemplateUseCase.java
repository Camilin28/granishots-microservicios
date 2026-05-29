package com.granishots.notifications.domain.usecase;

import com.granishots.notifications.domain.exception.BusinessException;
import com.granishots.notifications.domain.model.NotificationTemplate;
import com.granishots.notifications.domain.model.gateway.TemplateGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class TemplateUseCase {

    private final TemplateGateway templateGateway;

    private static final List<String> TIPOS_VALIDOS = List.of(
            "ORDER_CONFIRMED","ORDER_READY","PROMO","REMINDER","WELCOME","CANCELLED");
    private static final List<String> CANALES_VALIDOS = List.of("EMAIL","SMS","PUSH","WHATSAPP");
    private static final int BODY_MAX = 2000;
    private static final int NOMBRE_MAX = 100;

    public NotificationTemplate save(NotificationTemplate template) {
        validarTemplate(template);
        template.setName(template.getName().trim());
        template.setType(template.getType().trim().toUpperCase());
        template.setChannel(template.getChannel().trim().toUpperCase());
        template.setActive(true);
        return templateGateway.save(template);
    }

    public NotificationTemplate findById(Long id) {
        validarId(id);
        NotificationTemplate t = templateGateway.findById(id);
        if (t == null)
            throw new BusinessException("No existe una plantilla con el id: " + id, 404);
        return t;
    }

    public List<NotificationTemplate> findAll() { return templateGateway.findAll(); }

    public List<NotificationTemplate> findByType(String type) {
        if (type == null || type.trim().isEmpty())
            throw new BusinessException("El tipo no puede estar vacío", 400);
        String t = type.trim().toUpperCase();
        if (!TIPOS_VALIDOS.contains(t))
            throw new BusinessException("Tipo inválido: '" + type + "'. Válidos: " + TIPOS_VALIDOS, 400);
        return templateGateway.findByType(t);
    }

    public List<NotificationTemplate> findByChannel(String channel) {
        if (channel == null || channel.trim().isEmpty())
            throw new BusinessException("El canal no puede estar vacío", 400);
        String c = channel.trim().toUpperCase();
        if (!CANALES_VALIDOS.contains(c))
            throw new BusinessException("Canal inválido: '" + channel + "'. Válidos: " + CANALES_VALIDOS, 400);
        return templateGateway.findByChannel(c);
    }

    public NotificationTemplate update(Long id, NotificationTemplate template) {
        validarId(id);
        validarTemplate(template);
        if (templateGateway.findById(id) == null)
            throw new BusinessException("No existe una plantilla con el id: " + id, 404);
        template.setId(id);
        template.setName(template.getName().trim());
        template.setType(template.getType().trim().toUpperCase());
        template.setChannel(template.getChannel().trim().toUpperCase());
        return templateGateway.update(template);
    }

    public void deleteById(Long id) {
        validarId(id);
        if (templateGateway.findById(id) == null)
            throw new BusinessException("No existe una plantilla con el id: " + id, 404);
        templateGateway.deleteById(id);
    }

    private void validarId(Long id) {
        if (id == null) throw new BusinessException("El ID no puede ser nulo", 400);
        if (id <= 0) throw new BusinessException("El ID debe ser positivo. Recibido: " + id, 400);
    }

    private void validarTemplate(NotificationTemplate t) {
        if (t == null) throw new BusinessException("El cuerpo de la plantilla no puede ser nulo", 400);
        if (t.getName() == null || t.getName().trim().isEmpty())
            throw new BusinessException("El nombre de la plantilla es obligatorio", 400);
        if (t.getName().trim().length() > NOMBRE_MAX)
            throw new BusinessException("El nombre no puede superar " + NOMBRE_MAX + " caracteres", 400);
        if (t.getType() == null || t.getType().trim().isEmpty())
            throw new BusinessException("El tipo de plantilla es obligatorio", 400);
        if (!TIPOS_VALIDOS.contains(t.getType().trim().toUpperCase()))
            throw new BusinessException("Tipo inválido: '" + t.getType() + "'. Válidos: " + TIPOS_VALIDOS, 400);
        if (t.getChannel() == null || t.getChannel().trim().isEmpty())
            throw new BusinessException("El canal de plantilla es obligatorio", 400);
        if (!CANALES_VALIDOS.contains(t.getChannel().trim().toUpperCase()))
            throw new BusinessException("Canal inválido: '" + t.getChannel() + "'. Válidos: " + CANALES_VALIDOS, 400);
        if (t.getBody() == null || t.getBody().trim().isEmpty())
            throw new BusinessException("El cuerpo de la plantilla es obligatorio", 400);
        if (t.getBody().trim().length() > BODY_MAX)
            throw new BusinessException("El cuerpo no puede superar " + BODY_MAX + " caracteres. Actual: " + t.getBody().length(), 400);
    }
}
