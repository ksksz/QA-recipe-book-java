const qs = (id) => document.getElementById(id);

const flags = ['vegan', 'glutenFree', 'sugarFree'];
const maxPhotos = 5;
const flagLabels = {
  vegan: 'Веган',
  glutenFree: 'Без глютена',
  sugarFree: 'Без сахара',
};

const preparedAssets = [
  { kind: 'product', title: 'Свёкла', path: './assets/library/product-beet.png' },
  { kind: 'product', title: 'Картофель', path: './assets/library/product-potato.png' },
  { kind: 'product', title: 'Вода', path: './assets/library/product-water.png' },
  { kind: 'product', title: 'Мясо', path: './assets/library/product-meat.jpeg' },
  { kind: 'product', title: 'Мясо, фото 2', path: './assets/library/product-meat-alt.jpeg' },
  { kind: 'product', title: 'Пончики', path: './assets/library/product-donuts.png' },
  { kind: 'product', title: 'Тыква', path: './assets/library/product-pumpkin.png' },
  { kind: 'product', title: 'Дополнительный продукт', path: './assets/library/product-extra-food.png' },
  { kind: 'product', title: 'Овощной набор', path: './assets/library/product-vegetable-set.png' },
  { kind: 'dish', title: 'Борщ', path: './assets/library/dish-borscht.png' },
  { kind: 'dish', title: 'Борщ веганский', path: './assets/library/dish-borscht-vegan.png' },
];

const fallbackMeta = {
  PRODUCT_CATEGORIES: ['Замороженный', 'Мясной', 'Овощи', 'Зелень', 'Специи', 'Крупы', 'Консервы', 'Жидкость', 'Сладости'],
  COOKING_NEED: ['Готовый к употреблению', 'Полуфабрикат', 'Требует приготовления'],
  DISH_CATEGORIES: ['Десерт', 'Первое', 'Второе', 'Напиток', 'Салат', 'Суп', 'Перекус'],
  FLAGS: flags,
};

const localDbKey = 'recipe-book-local-db-v1';
const localSeedDb = {
  products: [
    {
      id: 'defense-product-beet',
      name: 'Свёкла',
      photos: ['./assets/library/product-beet.png'],
      calories: 43,
      proteins: 1.6,
      fats: 0.2,
      carbs: 9.6,
      compositionText: null,
      category: 'Овощи',
      cookingNeed: 'Требует приготовления',
      flags: { vegan: true, glutenFree: true, sugarFree: true },
      createdAt: '2026-05-08T00:00:00.000Z',
      updatedAt: null,
    },
    {
      id: 'defense-product-potato',
      name: 'Картофель',
      photos: ['./assets/library/product-potato.png'],
      calories: 77,
      proteins: 2,
      fats: 0.4,
      carbs: 16.3,
      compositionText: null,
      category: 'Овощи',
      cookingNeed: 'Требует приготовления',
      flags: { vegan: true, glutenFree: true, sugarFree: true },
      createdAt: '2026-05-08T00:00:00.000Z',
      updatedAt: null,
    },
    {
      id: 'defense-product-water',
      name: 'Вода',
      photos: ['./assets/library/product-water.png'],
      calories: 0,
      proteins: 0,
      fats: 0,
      carbs: 0,
      compositionText: null,
      category: 'Жидкость',
      cookingNeed: 'Готовый к употреблению',
      flags: { vegan: true, glutenFree: true, sugarFree: true },
      createdAt: '2026-05-08T00:00:00.000Z',
      updatedAt: null,
    },
    {
      id: 'defense-product-meat',
      name: 'Мясо',
      photos: ['./assets/library/product-meat.jpeg', './assets/library/product-meat-alt.jpeg'],
      calories: 187.2,
      proteins: 18.9,
      fats: 12.4,
      carbs: 0,
      compositionText: null,
      category: 'Мясной',
      cookingNeed: 'Требует приготовления',
      flags: { vegan: false, glutenFree: true, sugarFree: true },
      createdAt: '2026-05-08T00:00:00.000Z',
      updatedAt: null,
    },
  ],
  dishes: [
    {
      id: 'defense-dish-potato-stew',
      name: 'Похлёбка картофельная',
      photos: [],
      calories: 154,
      proteins: 4,
      fats: 0.8,
      carbs: 32.6,
      nutritionDraft: { calories: 154, proteins: 4, fats: 0.8, carbs: 32.6 },
      composition: [
        { productId: 'defense-product-potato', amount: 200 },
        { productId: 'defense-product-water', amount: 500 },
      ],
      portionSize: 700,
      category: 'Суп',
      flags: { vegan: true, glutenFree: true, sugarFree: true },
      availableFlags: { vegan: true, glutenFree: true, sugarFree: true },
      createdAt: '2026-05-08T00:00:00.000Z',
      updatedAt: null,
    },
    {
      id: 'defense-dish-borscht',
      name: 'Борщ',
      photos: ['./assets/library/dish-borscht.png'],
      calories: 384.2,
      proteins: 24.5,
      fats: 13.4,
      carbs: 42.2,
      nutritionDraft: { calories: 384.2, proteins: 24.5, fats: 13.4, carbs: 42.2 },
      composition: [
        { productId: 'defense-product-beet', amount: 100 },
        { productId: 'defense-product-potato', amount: 200 },
        { productId: 'defense-product-water', amount: 500 },
        { productId: 'defense-product-meat', amount: 100 },
      ],
      portionSize: 900,
      category: 'Суп',
      flags: { vegan: false, glutenFree: true, sugarFree: true },
      availableFlags: { vegan: false, glutenFree: true, sugarFree: true },
      createdAt: '2026-05-08T00:00:00.000Z',
      updatedAt: null,
    },
    {
      id: 'defense-dish-borscht-vegan',
      name: 'Борщ веганский',
      photos: ['./assets/library/dish-borscht-vegan.png'],
      calories: 197,
      proteins: 5.6,
      fats: 1,
      carbs: 42.2,
      nutritionDraft: { calories: 197, proteins: 5.6, fats: 1, carbs: 42.2 },
      composition: [
        { productId: 'defense-product-beet', amount: 100 },
        { productId: 'defense-product-potato', amount: 200 },
        { productId: 'defense-product-water', amount: 500 },
      ],
      portionSize: 800,
      category: 'Суп',
      flags: { vegan: true, glutenFree: true, sugarFree: true },
      availableFlags: { vegan: true, glutenFree: true, sugarFree: true },
      createdAt: '2026-05-08T00:00:00.000Z',
      updatedAt: null,
    },
  ],
};

const state = {
  meta: null,
  productsCache: [],
  editingProductId: null,
  editingDishId: null,
  dishManualNutrition: { calories: false, proteins: false, fats: false, carbs: false },
  lastDishCompositionSignature: '',
};

function formatValidationError(errorPayload) {
  const fieldErrors = errorPayload && errorPayload.fieldErrors;
  if (!fieldErrors || typeof fieldErrors !== 'object') return null;
  const lines = Object.entries(fieldErrors)
    .filter(([, messages]) => Array.isArray(messages) && messages.length)
    .map(([field, messages]) => `${field}: ${messages.join(', ')}`);
  return lines.length ? lines.join('\n') : null;
}

function formatHttpErrorText(status, responseText) {
  const normalized = (responseText || '').trim();
  if (!normalized) return `HTTP ${status}`;
  if (/^<!doctype html/i.test(normalized) || /^<html[\s>]/i.test(normalized)) {
    return `HTTP ${status}: API вернул HTML-страницу вместо данных. Проверьте, что сервер запущен из проекта и доступен по localhost:3000.`;
  }
  return normalized;
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function readLocalDb() {
  const raw = localStorage.getItem(localDbKey);
  if (!raw) {
    const seeded = clone(localSeedDb);
    localStorage.setItem(localDbKey, JSON.stringify(seeded));
    return seeded;
  }
  try {
    const parsed = JSON.parse(raw);
    const sanitized = sanitizeLocalDb(parsed);
    if (JSON.stringify(parsed) !== JSON.stringify(sanitized)) {
      localStorage.setItem(localDbKey, JSON.stringify(sanitized));
    }
    return sanitized;
  } catch (error) {
    const fallbackSeeded = clone(localSeedDb);
    localStorage.setItem(localDbKey, JSON.stringify(fallbackSeeded));
    return fallbackSeeded;
  }
}

function sanitizeLocalPhotos(photos) {
  return (photos || []).filter((photo) => typeof photo === 'string' && !photo.startsWith('data:image/'));
}

function sanitizeLocalDb(db) {
  return Object.assign({}, db, {
    products: (db.products || []).map((product) => Object.assign({}, product, {
      photos: sanitizeLocalPhotos(product.photos),
    })),
    dishes: (db.dishes || []).map((dish) => Object.assign({}, dish, {
      photos: sanitizeLocalPhotos(dish.photos),
    })),
  });
}

function writeLocalDb(db) {
  const sanitized = sanitizeLocalDb(db);
  localStorage.setItem(localDbKey, JSON.stringify(sanitized));
}

function localId(prefix) {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function parseLocalValue(value, fallback) {
  if (value == null || value === '') return fallback;
  if (typeof value !== 'string') return value;
  try {
    return JSON.parse(value);
  } catch (error) {
    return value;
  }
}

function createLocalPhotoUrl(file) {
  return URL.createObjectURL(file);
}

async function localPayload(body) {
  if (body instanceof FormData) {
    const data = {};
    const photoFiles = [];
    body.forEach((value, key) => {
      if (key === 'photosFiles') {
        if (value instanceof File && value.size > 0) photoFiles.push(value);
        return;
      }
      data[key] = parseLocalValue(value, value);
    });
    if (photoFiles.length) {
      const currentPhotos = Array.isArray(data.photos) ? data.photos : [];
      const uploadedPhotos = photoFiles.map((file) => createLocalPhotoUrl(file));
      data.photos = currentPhotos.concat(uploadedPhotos);
    }
    return data;
  }
  return parseLocalValue(body, body) || {};
}

const nutritionFieldLabels = {
  calories: 'Калории',
  proteins: 'Белки',
  fats: 'Жиры',
  carbs: 'Углеводы',
};

function assertLocalNonNegativeNutrition(data, fields, optional = false) {
  fields.forEach((field) => {
    if (optional && (data[field] == null || data[field] === '')) return;
    const value = Number(data[field]);
    if (!Number.isFinite(value) || value < 0) {
      throw new Error(`${nutritionFieldLabels[field]}: значение не может быть отрицательным`);
    }
  });
}

function normalizeLocalProductNutrition(data) {
  assertLocalNonNegativeNutrition(data, ['calories', 'proteins', 'fats', 'carbs']);
  return Object.assign({}, data, {
    calories: Number(data.calories),
    proteins: Number(data.proteins),
    fats: Number(data.fats),
    carbs: Number(data.carbs),
  });
}

function normalizeLocalDishNutrition(data) {
  assertLocalNonNegativeNutrition(data, ['calories', 'proteins', 'fats', 'carbs'], true);
  return ['calories', 'proteins', 'fats', 'carbs'].reduce(
    (acc, field) => {
      if (data[field] != null && data[field] !== '') acc[field] = Number(data[field]);
      return acc;
    },
    Object.assign({}, data)
  );
}

function localNutrition(composition, productsById) {
  return composition.reduce(
    (acc, row) => {
      const product = productsById.get(row.productId);
      if (!product) return acc;
      const ratio = row.amount / 100;
      acc.calories += product.calories * ratio;
      acc.proteins += product.proteins * ratio;
      acc.fats += product.fats * ratio;
      acc.carbs += product.carbs * ratio;
      return acc;
    },
    { calories: 0, proteins: 0, fats: 0, carbs: 0 }
  );
}

function localAvailableFlags(composition, productsById) {
  if (!composition.length) return { vegan: false, glutenFree: false, sugarFree: false };
  return flags.reduce((acc, flag) => {
    acc[flag] = composition.every((row) => {
      const product = productsById.get(row.productId);
      return Boolean(product && product.flags && product.flags[flag]);
    });
    return acc;
  }, {});
}

function localQuery(url) {
  const parsed = new URL(url, window.location.href);
  return { pathname: parsed.pathname, params: parsed.searchParams };
}

function filterAndSortProducts(products, params) {
  let list = products.slice();
  if (params.get('category')) list = list.filter((p) => p.category === params.get('category'));
  if (params.get('cookingNeed')) list = list.filter((p) => p.cookingNeed === params.get('cookingNeed'));
  if (params.get('search')) {
    const q = params.get('search').toLowerCase();
    list = list.filter((p) => p.name.toLowerCase().includes(q));
  }
  flags.forEach((flag) => {
    if (params.get(flag) === 'true') list = list.filter((p) => p.flags && p.flags[flag]);
  });
  const sort = params.get('sort');
  if (['name', 'calories', 'proteins', 'fats', 'carbs'].includes(sort)) {
    list.sort((a, b) => (sort === 'name' ? a.name.localeCompare(b.name, 'ru') : a[sort] - b[sort]));
  }
  return list;
}

function filterAndSortDishes(dishes, params) {
  let list = dishes.slice();
  if (params.get('category')) list = list.filter((d) => d.category === params.get('category'));
  if (params.get('search')) {
    const q = params.get('search').toLowerCase();
    list = list.filter((d) => d.name.toLowerCase().includes(q));
  }
  flags.forEach((flag) => {
    if (params.get(flag) === 'true') list = list.filter((d) => d.flags && d.flags[flag]);
  });
  const sort = params.get('sort');
  if (['name', 'calories', 'proteins', 'fats', 'carbs', 'portionSize'].includes(sort)) {
    list.sort((a, b) => (sort === 'name' ? a.name.localeCompare(b.name, 'ru') : a[sort] - b[sort]));
  }
  return list;
}

async function localApi(url, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const { pathname, params } = localQuery(url);
  const db = readLocalDb();

  if (pathname === '/api/meta') return fallbackMeta;

  if (pathname === '/api/products' && method === 'GET') return filterAndSortProducts(db.products, params);
  if (pathname === '/api/products' && method === 'POST') {
    const data = normalizeLocalProductNutrition(await localPayload(options.body));
    if (data.proteins + data.fats + data.carbs > 100) {
      throw new Error('macrosTotal: Сумма белков, жиров и углеводов не может превышать 100 г на 100 г продукта');
    }
    const product = {
      id: localId('product'),
      name: data.name.trim(),
      photos: data.photos || [],
      calories: Number(data.calories),
      proteins: Number(data.proteins),
      fats: Number(data.fats),
      carbs: Number(data.carbs),
      compositionText: data.compositionText || null,
      category: data.category,
      cookingNeed: data.cookingNeed,
      flags: data.flags || {},
      createdAt: new Date().toISOString(),
      updatedAt: null,
    };
    db.products.push(product);
    writeLocalDb(db);
    return product;
  }

  const productMatch = pathname.match(/^\/api\/products\/(.+)$/);
  if (productMatch) {
    const id = productMatch[1];
    const idx = db.products.findIndex((p) => p.id === id);
    if (idx < 0) throw new Error('Product not found');
    if (method === 'GET') return db.products[idx];
    if (method === 'PUT') {
      const data = normalizeLocalProductNutrition(await localPayload(options.body));
      if (data.proteins + data.fats + data.carbs > 100) {
        throw new Error('macrosTotal: Сумма белков, жиров и углеводов не может превышать 100 г на 100 г продукта');
      }
      db.products[idx] = Object.assign({}, db.products[idx], data, { name: data.name.trim(), updatedAt: new Date().toISOString() });
      writeLocalDb(db);
      return db.products[idx];
    }
    if (method === 'DELETE') {
      const usage = db.dishes.filter((dish) => dish.composition.some((row) => row.productId === id));
      if (usage.length) {
        const error = new Error('Cannot delete product used in dishes');
        error.payload = { dishes: usage.map((dish) => ({ id: dish.id, name: dish.name })) };
        throw error;
      }
      db.products.splice(idx, 1);
      writeLocalDb(db);
      return null;
    }
  }

  if (pathname === '/api/dishes' && method === 'GET') return filterAndSortDishes(db.dishes, params);
  if (pathname === '/api/dishes' && method === 'POST') {
    const data = normalizeLocalDishNutrition(await localPayload(options.body));
    const productsById = new Map(db.products.map((p) => [p.id, p]));
    const composition = (data.composition || []).map((row) => Object.assign({}, row, { amount: Number(row.amount) }));
    const draft = localNutrition(composition, productsById);
    const available = localAvailableFlags(composition, productsById);
    const requested = data.flags || {};
    const dish = {
      id: localId('dish'),
      name: data.name.trim().replace(/\s*!(десерт|первое|второе|напиток|салат|суп|перекус)\s*/gi, ' ').replace(/\s{2,}/g, ' ').trim(),
      photos: data.photos || [],
      calories: data.calories == null ? draft.calories : Number(data.calories),
      proteins: data.proteins == null ? draft.proteins : Number(data.proteins),
      fats: data.fats == null ? draft.fats : Number(data.fats),
      carbs: data.carbs == null ? draft.carbs : Number(data.carbs),
      nutritionDraft: draft,
      composition,
      portionSize: Number(data.portionSize),
      category: data.category || 'Суп',
      flags: flags.reduce((acc, flag) => {
        acc[flag] = available[flag] ? Boolean(requested[flag]) : false;
        return acc;
      }, {}),
      availableFlags: available,
      createdAt: new Date().toISOString(),
      updatedAt: null,
    };
    db.dishes.push(dish);
    writeLocalDb(db);
    return dish;
  }

  const dishMatch = pathname.match(/^\/api\/dishes\/(.+)$/);
  if (dishMatch) {
    const id = dishMatch[1];
    const idx = db.dishes.findIndex((d) => d.id === id);
    if (idx < 0) throw new Error('Dish not found');
    if (method === 'GET') {
      const productsById = new Map(db.products.map((p) => [p.id, p]));
      return Object.assign({}, db.dishes[idx], {
        compositionDetailed: db.dishes[idx].composition.map((row) => Object.assign({}, row, { product: productsById.get(row.productId) || null })),
      });
    }
    if (method === 'PUT') {
      const data = normalizeLocalDishNutrition(await localPayload(options.body));
      const productsById = new Map(db.products.map((p) => [p.id, p]));
      const composition = (data.composition || []).map((row) => Object.assign({}, row, { amount: Number(row.amount) }));
      const draft = localNutrition(composition, productsById);
      const available = localAvailableFlags(composition, productsById);
      const requested = data.flags || {};
      db.dishes[idx] = Object.assign({}, db.dishes[idx], {
        name: data.name.trim().replace(/\s*!(десерт|первое|второе|напиток|салат|суп|перекус)\s*/gi, ' ').replace(/\s{2,}/g, ' ').trim(),
        photos: data.photos || [],
        calories: data.calories == null ? draft.calories : Number(data.calories),
        proteins: data.proteins == null ? draft.proteins : Number(data.proteins),
        fats: data.fats == null ? draft.fats : Number(data.fats),
        carbs: data.carbs == null ? draft.carbs : Number(data.carbs),
        nutritionDraft: draft,
        composition,
        portionSize: Number(data.portionSize),
        category: data.category || db.dishes[idx].category,
        flags: flags.reduce((acc, flag) => {
          acc[flag] = available[flag] ? Boolean(requested[flag]) : false;
          return acc;
        }, {}),
        availableFlags: available,
        updatedAt: new Date().toISOString(),
      });
      writeLocalDb(db);
      return db.dishes[idx];
    }
    if (method === 'DELETE') {
      db.dishes.splice(idx, 1);
      writeLocalDb(db);
      return null;
    }
  }

  throw new Error(`Local API route not found: ${pathname}`);
}

async function loadMeta() {
  try {
    return await api('/api/meta');
  } catch (error) {
    console.warn(error.message);
    return fallbackMeta;
  }
}

async function api(url, options = {}) {
  if (window.location.protocol === 'file:') {
    return localApi(url, options);
  }

  const isFormData = options.body instanceof FormData;
  let res;
  try {
    res = await fetch(url, Object.assign({}, options, {
      headers: isFormData ? {} : { 'Content-Type': 'application/json' },
    }));
  } catch (error) {
    return localApi(url, options);
  }

  const responseText = res.status === 204 ? '' : await res.text();
  let payload = null;
  if (responseText) {
    try {
      payload = JSON.parse(responseText);
    } catch (error) {
      payload = null;
    }
  }

  if (!res.ok) {
    if (
      [404, 405].includes(res.status) ||
      /^<!doctype html/i.test(responseText.trim()) ||
      /^<html[\s>]/i.test(responseText.trim())
    ) {
      return localApi(url, options);
    }
    const readableValidation = formatValidationError(payload && payload.error);
    const message =
      readableValidation ||
      (payload && typeof payload.error === 'string' ? payload.error : null) ||
      formatHttpErrorText(res.status, responseText);
    const error = new Error(message);
    error.payload = payload;
    throw error;
  }

  if (res.status === 204) return null;
  return payload;
}

function renderFlagsByState(stateObj) {
  return flags
    .filter((flag) => stateObj && stateObj[flag])
    .map((flag) => flagLabels[flag])
    .join(', ') || 'нет';
}

function splitPhotos(value) {
  if (!value) return [];
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

function setPhotos(prefix, photos) {
  const input = qs(`${prefix}-photos`);
  if (!input) return;
  input.value = photos.join(', ');
  renderEditablePhotos(prefix);
}

function renderEditablePhotos(prefix) {
  const root = qs(`${prefix}-current-photos`);
  const input = qs(`${prefix}-photos`);
  if (!root || !input) return;

  const photos = splitPhotos(input.value);
  root.innerHTML = '';

  if (!photos.length) {
    root.innerHTML = '<div class="empty-state compact">Фото пока не добавлены.</div>';
    return;
  }

  photos.forEach((photo, index) => {
    const item = document.createElement('div');
    item.className = 'editable-photo-item';
    item.innerHTML = `
      <img src="${photo}" alt="Фото ${index + 1}" />
      <a href="${photo}" target="_blank" rel="noreferrer">${photo}</a>
      <button type="button" class="secondary" data-photo-remove="${prefix}" data-photo-index="${index}">Удалить</button>
    `;
    root.append(item);
  });
}

function bindEditablePhotos(prefix) {
  const photosInput = qs(`${prefix}-photos`);
  if (photosInput) photosInput.addEventListener('input', () => renderEditablePhotos(prefix));
  const currentPhotos = qs(`${prefix}-current-photos`);
  if (currentPhotos) currentPhotos.addEventListener('click', (event) => {
    const button = event.target.closest(`[data-photo-remove="${prefix}"]`);
    if (!button) return;

    const photosField = qs(`${prefix}-photos`);
    const photos = splitPhotos(photosField ? photosField.value : '');
    photos.splice(Number(button.dataset.photoIndex), 1);
    setPhotos(prefix, photos);
  });
  renderEditablePhotos(prefix);
}

function makeFormData(payload, filesInputId) {
  const input = qs(filesInputId);
  const selectedFiles = Array.from(input ? input.files : []);
  const existingPhotos = Array.isArray(payload.photos) ? payload.photos : [];
  if (existingPhotos.length + selectedFiles.length > maxPhotos) {
    throw new Error(`Можно добавить не более ${maxPhotos} фото`);
  }

  const formData = new FormData();
  Object.entries(payload).forEach(([key, value]) => {
    if (value == null) return;
    if (typeof value === 'object') {
      formData.append(key, JSON.stringify(value));
      return;
    }
    formData.append(key, String(value));
  });

  selectedFiles.forEach((file) => formData.append('photosFiles', file));
  return formData;
}

function renderPhotoLinks(photos) {
  if (!photos || !photos.length) return 'нет';
  return photos
    .map((photo) => `<a href="${photo}" target="_blank" rel="noreferrer">${photo}</a>`)
    .join('<br/>');
}

function renderPhotoGallery(photos, alt) {
  if (!photos || !photos.length) return '';
  return `
    <div class="photo-gallery">
      ${photos
        .map((photo, index) => `<img src="${photo}" alt="${alt} ${index + 1}" class="photo-preview" />`)
        .join('')}
    </div>
  `;
}

function formatCompositionText(composition) {
  return composition
    .map((row) => {
      const product = state.productsCache.find((item) => item.id === row.productId);
      return `${product ? product.name : row.productId}: ${row.amount} г`;
    })
    .join('\n');
}

function fillDishProductSelect() {
  const select = qs('d-product-select');
  if (!select) return;

  select.innerHTML = '<option value="">Выберите продукт</option>';
  state.productsCache
    .slice()
    .sort((a, b) => a.name.localeCompare(b.name, 'ru'))
    .forEach((product) => {
      const option = document.createElement('option');
      option.value = product.id;
      option.textContent = product.name;
      select.append(option);
    });

  select.disabled = state.productsCache.length === 0;
}

function setDishComposition(composition) {
  const input = qs('d-composition');
  if (!input) return;
  input.value = formatCompositionText(composition);
  renderDishCompositionList();
  updateDishDerivedFields();
}

function renderDishCompositionList() {
  const root = qs('d-composition-list');
  if (!root) return;

  const compositionField = qs('d-composition');
  const composition = parseCompositionSafe(compositionField ? compositionField.value : '');
  root.innerHTML = '';

  if (!composition.length) {
    root.innerHTML = '<div class="empty-state compact">Состав пока пуст.</div>';
    return;
  }

  composition.forEach((row, index) => {
    const product = state.productsCache.find((item) => item.id === row.productId);
    const item = document.createElement('div');
    item.className = 'composition-row';
    item.innerHTML = `
      <span>${product ? product.name : row.productId}</span>
      <label>
        <input
          type="number"
          step="0.01"
          min="0.01"
          value="${row.amount}"
          data-composition-amount="${index}"
        />
        <small>г</small>
      </label>
      <button type="button" class="secondary" data-composition-remove="${index}">Удалить</button>
    `;
    root.append(item);
  });
}

function addDishProductToComposition() {
  const productSelect = qs('d-product-select');
  const productId = productSelect ? productSelect.value : '';
  const productAmount = qs('d-product-amount');
  const amount = Number(productAmount ? productAmount.value : '');

  if (!productId) {
    alert('Выберите продукт для блюда');
    return;
  }
  if (!Number.isFinite(amount) || amount <= 0) {
    alert('Укажите количество продукта больше 0');
    return;
  }

  const compositionField = qs('d-composition');
  const composition = parseCompositionSafe(compositionField ? compositionField.value : '');
  const existing = composition.find((row) => row.productId === productId);
  if (existing) existing.amount = Number((existing.amount + amount).toFixed(2));
  else composition.push({ productId, amount });

  if (qs('d-product-amount')) qs('d-product-amount').value = '';
  setDishComposition(composition);
}

function readTextComposition(raw) {
  const text = raw.trim();
  if (!text) throw new Error('Состав не должен быть пустым');

  const lines = text
    .split(/\n|;/)
    .map((line) => line.trim())
    .filter(Boolean);

  if (!lines.length) throw new Error('Состав не должен быть пустым');

  return lines.map((line) => {
    const parts = line.split(':');
    if (parts.length < 2) {
      throw new Error('Формат состава: "Название продукта: количество"');
    }

    const productRef = parts[0].trim();
    const rawAmount = parts
      .slice(1)
      .join(':')
      .replace(',', '.')
      .replace(/[^\d.]/g, '')
      .trim();
    const amount = Number(rawAmount);
    if (!Number.isFinite(amount) || amount <= 0) {
      throw new Error(`Некорректное количество в строке: "${line}"`);
    }

    const product = state.productsCache.find(
      (item) => item.name.toLowerCase() === productRef.toLowerCase() || item.id === productRef
    );
    if (!product) {
      throw new Error(`Продукт не найден: "${productRef}"`);
    }

    return { productId: product.id, amount };
  });
}

function parseCompositionSafe(raw) {
  try {
    return readTextComposition(raw);
  } catch (error) {
    return [];
  }
}

function computeDishDraft(composition) {
  return composition.reduce(
    (acc, row) => {
      const product = state.productsCache.find((item) => item.id === row.productId);
      if (!product) return acc;
      const ratio = row.amount / 100;
      acc.calories += product.calories * ratio;
      acc.proteins += product.proteins * ratio;
      acc.fats += product.fats * ratio;
      acc.carbs += product.carbs * ratio;
      return acc;
    },
    { calories: 0, proteins: 0, fats: 0, carbs: 0 }
  );
}

function computeDishFlagsAvailable(composition) {
  if (!composition.length) {
    return { vegan: false, glutenFree: false, sugarFree: false };
  }

  return flags.reduce((acc, key) => {
    acc[key] = composition.every((row) => {
      const product = state.productsCache.find((item) => item.id === row.productId);
      return Boolean(product && product.flags && product.flags[key]);
    });
    return acc;
  }, {});
}

function getCompositionSignature(composition) {
  return JSON.stringify(composition);
}

function fillSelect(id, values, includeAny = false, anyLabel = 'Любое') {
  const element = qs(id);
  if (!element) return;
  element.innerHTML = includeAny ? `<option value="">${anyLabel}</option>` : '';
  values.forEach((value) => {
    const option = document.createElement('option');
    option.value = value;
    option.textContent = value;
    element.append(option);
  });
}

function fillMeta() {
  if (!state.meta) return;
  fillSelect('p-category', state.meta.PRODUCT_CATEGORIES);
  fillSelect('p-filter-category', state.meta.PRODUCT_CATEGORIES, true, 'Любая категория');
  fillSelect('p-cooking', state.meta.COOKING_NEED);
  fillSelect('p-filter-cooking', state.meta.COOKING_NEED, true, 'Любая готовность');
  fillSelect('d-category', state.meta.DISH_CATEGORIES, true, 'Категория');
  fillSelect('d-filter-category', state.meta.DISH_CATEGORIES, true, 'Любая категория');
}

function renderPreparedAssets(rootId, kind) {
  const root = qs(rootId);
  if (!root) return;
  const assets = kind === 'all' ? preparedAssets : preparedAssets.filter((item) => item.kind === kind);
  root.innerHTML = '';

  assets.forEach((asset) => {
    const card = document.createElement('article');
    card.className = 'card asset-card';
    card.innerHTML = `
      <img src="${asset.path}" alt="${asset.title}" />
      <strong>${asset.title}</strong>
      <a class="asset-path" href="${asset.path}" target="_blank" rel="noreferrer">${asset.path}</a>
    `;
    root.append(card);
  });
}

function renderProductDetails(product) {
  return `
    <div class="card detail-block">
      ${renderPhotoGallery(product.photos, `Фото продукта ${product.name}`)}
      <div class="card-title">
        <strong>${product.name}</strong>
        <span class="badge">${product.category}</span>
      </div>
      Готовность: ${product.cookingNeed}<br/>
      КБЖУ (100 г): ${product.calories.toFixed(2)} / ${product.proteins.toFixed(2)} / ${product.fats.toFixed(2)} / ${product.carbs.toFixed(2)}<br/>
      Состав: ${product.compositionText || 'нет'}<br/>
      Флаги: ${renderFlagsByState(product.flags)}<br/>
      Фото:<br/>${renderPhotoLinks(product.photos)}<br/>
      Дата создания: ${product.createdAt}<br/>
      Дата редактирования: ${product.updatedAt || 'нет'}
    </div>
  `;
}

function renderDishDetails(dish) {
  const compositionDetailed = (dish.compositionDetailed || [])
    .map((row) => `${row.product ? row.product.name : row.productId}: ${row.amount} г`)
    .join('<br/>');
  return `
    <div class="card detail-block">
      ${renderPhotoGallery(dish.photos, `Фото блюда ${dish.name}`)}
      <div class="card-title">
        <strong>${dish.name}</strong>
        <span class="badge">${dish.category}</span>
      </div>
      Размер порции: ${dish.portionSize} г<br/>
      КБЖУ (порция): ${dish.calories.toFixed(2)} / ${dish.proteins.toFixed(2)} / ${dish.fats.toFixed(2)} / ${dish.carbs.toFixed(2)}<br/>
      Полный состав:<br/>${compositionDetailed || 'нет'}<br/>
      Флаги: ${renderFlagsByState(dish.flags)}<br/>
      Фото:<br/>${renderPhotoLinks(dish.photos)}<br/>
      Дата создания: ${dish.createdAt}<br/>
      Дата редактирования: ${dish.updatedAt || 'нет'}
    </div>
  `;
}

async function refreshProductsCache() {
  state.productsCache = await api('/api/products');
  fillDishProductSelect();
  renderDishCompositionList();
}

function productPayload() {
  return {
    name: qs('p-name').value,
    photos: splitPhotos(qs('p-photos').value),
    calories: Number(qs('p-calories').value),
    proteins: Number(qs('p-proteins').value),
    fats: Number(qs('p-fats').value),
    carbs: Number(qs('p-carbs').value),
    compositionText: qs('p-composition').value || null,
    category: qs('p-category').value,
    cookingNeed: qs('p-cooking').value,
    flags: {
      vegan: qs('p-vegan').checked,
      glutenFree: qs('p-glutenFree').checked,
      sugarFree: qs('p-sugarFree').checked,
    },
  };
}

function clearProductForm() {
  state.editingProductId = null;
  ['p-name', 'p-photos', 'p-calories', 'p-proteins', 'p-fats', 'p-carbs', 'p-composition'].forEach((id) => {
    const element = qs(id);
    if (element) element.value = '';
  });
  if (qs('p-photos-files')) qs('p-photos-files').value = '';
  flags.forEach((flag) => {
    const checkbox = qs(`p-${flag}`);
    if (checkbox) checkbox.checked = false;
  });
  renderEditablePhotos('p');
}

function loadProductToForm(product) {
  state.editingProductId = product.id;
  qs('p-name').value = product.name;
  qs('p-photos').value = (product.photos || []).join(', ');
  qs('p-photos-files').value = '';
  qs('p-calories').value = product.calories;
  qs('p-proteins').value = product.proteins;
  qs('p-fats').value = product.fats;
  qs('p-carbs').value = product.carbs;
  qs('p-composition').value = product.compositionText || '';
  qs('p-category').value = product.category;
  qs('p-cooking').value = product.cookingNeed;
  flags.forEach((flag) => {
    qs(`p-${flag}`).checked = Boolean(product.flags[flag]);
  });
  renderEditablePhotos('p');
  const createProduct = qs('create-product');
  if (createProduct) createProduct.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function refreshProductsList() {
  const root = qs('products');
  if (!root) return;

  const params = new URLSearchParams();
  if (qs('p-search') && qs('p-search').value) params.set('search', qs('p-search').value);
  if (qs('p-filter-category') && qs('p-filter-category').value) params.set('category', qs('p-filter-category').value);
  if (qs('p-filter-cooking') && qs('p-filter-cooking').value) params.set('cookingNeed', qs('p-filter-cooking').value);
  if (qs('p-sort') && qs('p-sort').value) params.set('sort', qs('p-sort').value);
  flags.forEach((flag) => {
    if (qs(`pf-${flag}`) && qs(`pf-${flag}`).checked) params.set(flag, 'true');
  });

  const products = await api(`/api/products?${params.toString()}`);
  root.innerHTML = '';

  if (!products.length) {
    root.innerHTML = '<div class="empty-state">Подходящих продуктов пока нет.</div>';
    return;
  }

  products.forEach((product) => {
    const card = document.createElement('article');
    card.className = 'card';
    card.innerHTML = `
      ${renderPhotoGallery(product.photos, `Фото продукта ${product.name}`)}
      <div class="card-title">
        <strong>${product.name}</strong>
        <span class="badge">${product.category}</span>
      </div>
      Готовность: ${product.cookingNeed}<br/>
      КБЖУ: ${product.calories.toFixed(2)} / ${product.proteins.toFixed(2)} / ${product.fats.toFixed(2)} / ${product.carbs.toFixed(2)}<br/>
      Флаги: ${renderFlagsByState(product.flags)}<br/>
      <div class="inline">
        <button type="button" data-product-view="${product.id}">Просмотреть</button>
        <button type="button" class="secondary" data-product-edit="${product.id}">Редактировать</button>
        <button type="button" class="secondary" data-product-delete="${product.id}">Удалить</button>
      </div>
      <div id="product-details-${product.id}"></div>
    `;
    root.append(card);

    card.querySelector('[data-product-view]').onclick = async () => {
      const detailsRoot = qs(`product-details-${product.id}`);
      if (detailsRoot.innerHTML.trim()) {
        detailsRoot.innerHTML = '';
        return;
      }
      const full = await api(`/api/products/${product.id}`);
      detailsRoot.innerHTML = renderProductDetails(full);
    };

    card.querySelector('[data-product-edit]').onclick = () => loadProductToForm(product);

    card.querySelector('[data-product-delete]').onclick = async () => {
      try {
        await api(`/api/products/${product.id}`, { method: 'DELETE' });
      } catch (error) {
        if (error && error.payload && error.payload.dishes && error.payload.dishes.length) {
          const dishNames = error.payload.dishes.map((dish) => `- ${dish.name}`).join('\n');
          alert(`Удаление невозможно: продукт используется в блюдах:\n${dishNames}`);
        } else {
          alert(`Ошибка удаления: ${error.message}`);
        }
      }
      await Promise.all([refreshProductsList(), refreshProductsCache()]);
    };
  });
}

function dishPayload() {
  const payload = {
    name: qs('d-name').value,
    photos: splitPhotos(qs('d-photos').value),
    composition: readTextComposition(qs('d-composition').value),
    portionSize: Number(qs('d-portion').value),
    flags: {
      vegan: qs('d-vegan').checked,
      glutenFree: qs('d-glutenFree').checked,
      sugarFree: qs('d-sugarFree').checked,
    },
  };

  if (qs('d-category').value) payload.category = qs('d-category').value;
  ['calories', 'proteins', 'fats', 'carbs'].forEach((field) => {
    const value = qs(`d-${field}`).value;
    if (value !== '') payload[field] = Number(value);
  });

  return payload;
}

function updateDishMacroLimits() {
  const portionInput = qs('d-portion');
  const portionSize = Number(portionInput ? portionInput.value : '');
  const hint = qs('d-macro-hint');
  ['proteins', 'fats', 'carbs'].forEach((field) => {
    const input = qs(`d-${field}`);
    if (!input) return;
    if (portionSize > 0) input.max = String(portionSize);
    else input.removeAttribute('max');
  });
  if (hint) {
    hint.textContent =
      portionSize > 0
        ? `Для блюда белки, жиры и углеводы не могут превышать ${portionSize} г каждый, а их сумма не должна быть больше размера порции.`
        : 'Сначала укажите размер порции, чтобы увидеть верхнюю границу БЖУ.';
  }
}

function updateDishDerivedFields() {
  if (!qs('d-composition')) return;

  updateDishMacroLimits();
  const composition = parseCompositionSafe(qs('d-composition').value);
  const compositionSignature = getCompositionSignature(composition);
  const compositionChanged = compositionSignature !== state.lastDishCompositionSignature;
  if (compositionChanged) {
    Object.keys(state.dishManualNutrition).forEach((key) => {
      state.dishManualNutrition[key] = false;
    });
    state.lastDishCompositionSignature = compositionSignature;
  }

  const draft = computeDishDraft(composition);
  const available = computeDishFlagsAvailable(composition);
  const hasComposition = composition.length > 0;

  flags.forEach((flag) => {
    qs(`d-${flag}`).disabled = !available[flag];
    if (!available[flag]) qs(`d-${flag}`).checked = false;
  });

  ['calories', 'proteins', 'fats', 'carbs'].forEach((field) => {
    if (!hasComposition && !state.dishManualNutrition[field]) {
      qs(`d-${field}`).value = '';
      return;
    }
    if (hasComposition && (!state.dishManualNutrition[field] || qs(`d-${field}`).value === '')) {
      qs(`d-${field}`).value = Number(draft[field].toFixed(2));
    }
  });
}

function clearDishForm() {
  state.editingDishId = null;
  ['d-name', 'd-photos', 'd-composition', 'd-portion', 'd-calories', 'd-proteins', 'd-fats', 'd-carbs'].forEach((id) => {
    const element = qs(id);
    if (element) element.value = '';
  });
  if (qs('d-photos-files')) qs('d-photos-files').value = '';
  if (qs('d-category')) qs('d-category').value = '';
  Object.keys(state.dishManualNutrition).forEach((key) => {
    state.dishManualNutrition[key] = false;
  });
  state.lastDishCompositionSignature = '';
  flags.forEach((flag) => {
    const checkbox = qs(`d-${flag}`);
    if (checkbox) checkbox.checked = false;
  });
  renderEditablePhotos('d');
  renderDishCompositionList();
  updateDishDerivedFields();
}

function loadDishToForm(dish) {
  state.editingDishId = dish.id;
  qs('d-name').value = dish.name;
  qs('d-photos').value = (dish.photos || []).join(', ');
  qs('d-photos-files').value = '';
  qs('d-composition').value = formatCompositionText(dish.composition);
  qs('d-portion').value = dish.portionSize;
  qs('d-category').value = dish.category;
  qs('d-calories').value = dish.calories;
  qs('d-proteins').value = dish.proteins;
  qs('d-fats').value = dish.fats;
  qs('d-carbs').value = dish.carbs;
  flags.forEach((flag) => {
    qs(`d-${flag}`).checked = Boolean(dish.flags[flag]);
    qs(`d-${flag}`).disabled = !dish.availableFlags[flag];
  });
  Object.keys(state.dishManualNutrition).forEach((key) => {
    state.dishManualNutrition[key] = true;
  });
  state.lastDishCompositionSignature = getCompositionSignature(parseCompositionSafe(qs('d-composition').value));
  renderEditablePhotos('d');
  renderDishCompositionList();
  updateDishMacroLimits();
  const createDish = qs('create-dish');
  if (createDish) createDish.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function refreshDishesList() {
  const root = qs('dishes');
  if (!root) return;

  const params = new URLSearchParams();
  if (qs('d-search') && qs('d-search').value) params.set('search', qs('d-search').value);
  if (qs('d-filter-category') && qs('d-filter-category').value) params.set('category', qs('d-filter-category').value);
  if (qs('d-sort') && qs('d-sort').value) params.set('sort', qs('d-sort').value);
  flags.forEach((flag) => {
    if (qs(`df-${flag}`) && qs(`df-${flag}`).checked) params.set(flag, 'true');
  });

  const dishes = await api(`/api/dishes?${params.toString()}`);
  root.innerHTML = '';

  if (!dishes.length) {
    root.innerHTML = '<div class="empty-state">Подходящих блюд пока нет.</div>';
    return;
  }

  dishes.forEach((dish) => {
    const composition = formatCompositionText(dish.composition).replace(/\n/g, '; ');
    const card = document.createElement('article');
    card.className = 'card';
    card.innerHTML = `
      ${renderPhotoGallery(dish.photos, `Фото блюда ${dish.name}`)}
      <div class="card-title">
        <strong>${dish.name}</strong>
        <span class="badge">${dish.category}</span>
      </div>
      Порция: ${dish.portionSize} г<br/>
      КБЖУ: ${dish.calories.toFixed(2)} / ${dish.proteins.toFixed(2)} / ${dish.fats.toFixed(2)} / ${dish.carbs.toFixed(2)}<br/>
      Состав: ${composition}<br/>
      Флаги: ${renderFlagsByState(dish.flags)}<br/>
      <div class="inline">
        <button type="button" data-dish-view="${dish.id}">Просмотреть</button>
        <button type="button" class="secondary" data-dish-edit="${dish.id}">Редактировать</button>
        <button type="button" class="secondary" data-dish-delete="${dish.id}">Удалить</button>
      </div>
      <div id="dish-details-${dish.id}"></div>
    `;
    root.append(card);

    card.querySelector('[data-dish-view]').onclick = async () => {
      const detailsRoot = qs(`dish-details-${dish.id}`);
      if (detailsRoot.innerHTML.trim()) {
        detailsRoot.innerHTML = '';
        return;
      }
      const full = await api(`/api/dishes/${dish.id}`);
      detailsRoot.innerHTML = renderDishDetails(full);
    };

    card.querySelector('[data-dish-edit]').onclick = () => loadDishToForm(dish);

    card.querySelector('[data-dish-delete]').onclick = async () => {
      await api(`/api/dishes/${dish.id}`, { method: 'DELETE' });
      await refreshDishesList();
    };
  });
}

function bindCopyButtons() {
  document.addEventListener('click', async (event) => {
    const button = event.target.closest('[data-copy-path]');
    if (!button) return;
    const originalText = button.textContent;
    try {
      await navigator.clipboard.writeText(button.dataset.copyPath);
      button.textContent = 'Скопировано';
      setTimeout(() => {
        button.textContent = originalText;
      }, 1200);
    } catch (error) {
      alert(`Не удалось скопировать путь:\n${button.dataset.copyPath}`);
    }
  });
}

async function initProductsPage() {
  state.meta = await loadMeta();
  fillMeta();
  renderPreparedAssets('prepared-product-assets', 'product');
  try {
    await Promise.all([refreshProductsCache(), refreshProductsList()]);
  } catch (error) {
    console.warn(error.message);
    if (qs('products')) {
      qs('products').innerHTML = '<div class="empty-state">Не удалось загрузить список продуктов. Проверьте, что сервер запущен.</div>';
    }
  }

  qs('p-save').onclick = async () => {
    try {
      const payload = productPayload();
      const formData = makeFormData(payload, 'p-photos-files');
      if (state.editingProductId) {
        await api(`/api/products/${state.editingProductId}`, { method: 'PUT', body: formData });
      } else {
        await api('/api/products', { method: 'POST', body: formData });
      }
      clearProductForm();
      await Promise.all([refreshProductsCache(), refreshProductsList()]);
    } catch (error) {
      alert(`Ошибка: ${error.message}`);
    }
  };

  qs('p-clear').onclick = clearProductForm;
  qs('p-refresh').onclick = refreshProductsList;
  bindEditablePhotos('p');
}

async function initDishesPage() {
  state.meta = await loadMeta();
  fillMeta();
  renderPreparedAssets('prepared-dish-assets', 'dish');
  renderPreparedAssets('prepared-product-assets-inline', 'product');
  try {
    await refreshProductsCache();
    await refreshDishesList();
  } catch (error) {
    console.warn(error.message);
    if (qs('dishes')) {
      qs('dishes').innerHTML = '<div class="empty-state">Не удалось загрузить список блюд. Проверьте, что сервер запущен.</div>';
    }
  }
  updateDishDerivedFields();

  qs('d-save').onclick = async () => {
    try {
      const payload = dishPayload();
      const formData = makeFormData(payload, 'd-photos-files');
      if (state.editingDishId) {
        await api(`/api/dishes/${state.editingDishId}`, { method: 'PUT', body: formData });
      } else {
      await api('/api/dishes', { method: 'POST', body: formData });
      }
      clearDishForm();
      await refreshDishesList();
    } catch (error) {
      alert(`Ошибка: ${error.message}`);
    }
  };

  qs('d-clear').onclick = clearDishForm;
  qs('d-refresh').onclick = refreshDishesList;
  bindEditablePhotos('d');
  qs('d-add-product').onclick = addDishProductToComposition;
  qs('d-product-amount').addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      addDishProductToComposition();
    }
  });
  qs('d-composition-list').addEventListener('click', (event) => {
    const button = event.target.closest('[data-composition-remove]');
    if (!button) return;
    const index = Number(button.dataset.compositionRemove);
    const compositionField = qs('d-composition');
    const composition = parseCompositionSafe(compositionField ? compositionField.value : '');
    composition.splice(index, 1);
    setDishComposition(composition);
  });
  qs('d-composition-list').addEventListener('input', (event) => {
    const input = event.target.closest('[data-composition-amount]');
    if (!input) return;

    const index = Number(input.dataset.compositionAmount);
    const amount = Number(input.value);
    if (!Number.isFinite(amount) || amount <= 0) return;

    const compositionField = qs('d-composition');
    const composition = parseCompositionSafe(compositionField ? compositionField.value : '');
    if (!composition[index]) return;
    composition[index].amount = amount;
    qs('d-composition').value = formatCompositionText(composition);
    updateDishDerivedFields();
  });
  qs('d-composition').addEventListener('input', () => {
    renderDishCompositionList();
    updateDishDerivedFields();
  });
  qs('d-portion').addEventListener('input', updateDishDerivedFields);
  ['calories', 'proteins', 'fats', 'carbs'].forEach((field) => {
    qs(`d-${field}`).addEventListener('input', () => {
      state.dishManualNutrition[field] = qs(`d-${field}`).value !== '';
    });
  });
}

(async function init() {
  bindCopyButtons();
  try {
    const page = document.body.dataset.page;
    if (page === 'products') {
      await initProductsPage();
      return;
    }
    if (page === 'dishes') {
      await initDishesPage();
      return;
    }
  } catch (error) {
    console.warn(error.message);
  }
})();
