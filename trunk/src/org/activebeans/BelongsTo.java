package org.activebeans;

import java.util.Map;

public interface BelongsTo<T extends Base<?>> {

	T get();

	T get(boolean forceReload);

	BelongsTo<T> set(T associate);

	T build(Map<String, ?> hash);

	T create(Map<String, ?> hash);

}
