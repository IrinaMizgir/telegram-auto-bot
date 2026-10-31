package org.mizgir.telegram.auto.bot.factory;

import java.util.Collections;
import org.mizgir.telegram.auto.bot.model.DataResult;
import org.mizgir.telegram.auto.bot.model.KeyboardData.KeyboardType;
import org.springframework.stereotype.Component;

/**
 * Фабрика для создания результата.
 *
 * @author Ирина Мизгир
 * @date 27.10.2026 19:10
 */
@Component
public class DataResultFactory {

  /**
   * Создание данные если авто не найдено.
   *
   * @return Данные.
   */
  public DataResult createInvalid() {
    return new DataResult(
        Collections.singleton(KeyboardType.REGISTER),
        """
            \uD83D\uDE97 Автомобиль не найден
            Похоже, вы ещё не добавили свой автомобиль в гараж.
            Давайте это исправим — это займёт меньше минуты!
            \uD83D\uDC47 Нажмите кнопку ниже, чтобы добавить автомобиль:""");
  }

  /**
   * Создание данные если результат валидный.
   *
   * @param message Сообщение.
   * @return Данные.
   */
  public DataResult createValid(String message) {
    return new DataResult(Collections.emptySet(), message);
  }
}
