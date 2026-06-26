# VanillaPlusProgression

Plugin Paper/Bukkit para Minecraft 1.21.4 basado en el GDD Vanilla+.

## Cambio agregado sobre el PDF

El PDF no especificaba la equivalencia base de poder de los tiers, así que esta versión lo aplica así:

- Sets Early: base de armadura/espada equivalente a diamante + habilidades pasivas.
- Sets Mid: base de armadura/espada equivalente a netherite + habilidades pasivas.
- Sets Late: base netherite + modificadores custom superiores a netherite mediante balance por eventos/configuración.

## Incluye

- 12 sets completos: casco, pechera, pantalones, botas, espada, arco/ballesta y amuleto.
- PDC estable con namespace `pp`.
- Recetas shaped y smithing registradas con NamespacedKey `pp:*`.
- Desbloqueos globales `/unlockpp early|mid|late|plantas|pesca|utils`.
- Bloqueo de recetas aunque el jugador intente fabricarlas manualmente.
- Home Crystal con 1 bloque de diamante + 4 pepitas de hierro.
- `/sethome`, `/home`, `/delhome`, `/homes`.
- `/tpa`, `/tpaccept`, `/tpdeny`.
- Modificadores pasivos de combate para explosiones, proyectiles, fuego, caída, espada, mazo y pociones.
- Drops custom desde mobs y bloques.
- Rework de pesca con rarezas y cooldown de legendarios.
- Sistema de plantas por ubicaciones registradas, sin escanear chunks.
- Configs editables: `config.yml`, `sets.yml`, `messages.yml`.

## Compilar

Requisitos:

- Java 21.
- Maven.

Comando:

```bash
mvn clean package
```

El jar queda en:

```bash
target/VanillaPlusProgression-1.0.0.jar
```

Luego se copia en:

```bash
server/plugins/
```

## Nota importante

Este entorno no tenía Maven instalado, así que el proyecto fue generado completo pero no pude compilarlo dentro del sandbox. El `pom.xml` ya trae Paper API 1.21.4-R0.1-SNAPSHOT como dependencia provided.
