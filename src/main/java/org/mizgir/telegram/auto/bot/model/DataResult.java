package org.mizgir.telegram.auto.bot.model;

import java.util.Set;
import org.mizgir.telegram.auto.bot.model.KeyboardData.KeyboardType;

/**
 * Информация текущих данных.
 *
 * @author Ирина Мизгир
 * @date 12.10.2026 21:59
 */
public record DataResult(Set<KeyboardType> types, String message) {

}
