/*
 * Copyright 2014 The LolDevs team (https://github.com/loldevs)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.boreeas.riotapi.spectator;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created 8/12/2014
 * @author Malte Schütze
 */
public class GamePool {
    private ScheduledExecutorService pool = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "Game pool timer thread");
        thread.setDaemon(true);
        return thread;
    });

    public void submit(InProgressGame game) {

        submit(game, err -> {});
    }

    public void submit(InProgressGame game, Consumer<Exception> errorCallback) {

        GameUpdateTask task = new GameUpdateTask(game, errorCallback);
        // Run once to pull all pending chunks, then run once
        task.run();
        ScheduledFuture<?> self = pool.scheduleAtFixedRate(task, game.getLastChunkInfo().getNextAvailableChunk(), game.getChunkInterval(), TimeUnit.MILLISECONDS);
        task.setSelf(self);
    }

    public void shutdown() {
        pool.shutdown();
    }

    public static GamePool singleton(InProgressGame game) {
        return singleton(game, err -> {});
    }

    public static GamePool singleton(InProgressGame game, Consumer<Exception> callback) {
        GamePool pool = new GamePool();
        pool.submit(game, callback);
        return pool;
    }
}
