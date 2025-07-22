import { useParams, Link } from "react-router-dom";
import products from "../data/products.json";
import productsApi from '../api/products';

export default function ProductPage() {
  const { id } = useParams();
  const product = products.find((p) => p.id === parseInt(id));

  if (!product) {
    return (
      <div className="p-6 text-center text-red-600">Product not found</div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Breadcrumb */}
      <div className="text-sm text-gray-500 mb-4">
        <Link to={`/`}>
          <span className="hover:underline cursor-pointer">Home</span> /{" "}
        </Link>
        <span className="hover:underline cursor-pointer">Category</span> /{" "}
        <span className="text-gray-700 font-semibold">{product.name}</span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Image Section (2/3) */}
        <div className="lg:col-span-2">
          {/* Main Image */}
          <img
            src={product.image}
            alt={product.name}
            className="w-full h-[420px] object-cover rounded-lg shadow"
          />
          {/* Mini Thumbnails */}
          <div className="flex gap-2 mt-2">
            {[...Array(4)].map((_, i) => (
              <img
                key={i}
                src={product.image}
                alt={`Thumbnail ${i + 1}`}
                className="w-20 h-20 object-cover rounded border cursor-pointer"
              />
            ))}
          </div>

          {/* Product Info */}
          <div className="mt-6">
            <h1 className="text-2xl font-bold">{product.name}</h1>
            <p className="text-sm text-gray-600 mt-2">{product.description}</p>
          </div>
        </div>

        {/* Info Section (1/3) */}
        <div className="border rounded-lg shadow p-6 space-y-4">
          {/* Rating & Reviews */}
          <div>
            <div className="text-yellow-500 font-bold text-lg">⭐ 4.7</div>
            <div className="text-sm text-gray-600">
              44 レビュー • 100以上注文されました
            </div>
          </div>

          {/* Price */}
          <div>
            <div className="text-4xl font-bold text-purple-700 mb-2">
              {product.price}￥
            </div>
            <div className="text-2xl text-gray-500">
              {Math.floor(product.price / 24)}円/月 24回払い
            </div>
          </div>

          {/* Color */}
          <div>
            <label x className="block text-sm font-medium">
              <select className="w-full border rounded p-2">
                <option>ブラック</option>
                <option>ホワイト</option>
                <option>ブルー</option>
              </select>
            </label>
          </div>

          {/* Size */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              サイズ
            </label>
            <div className="grid grid-cols-3 gap-2">
              {[
                "24cm",
                "24,5cm",
                "25cm",
                "25,5cm",
                "26cm",
                "26,5cm",
                "27cm",
                "27,5cm",
                "28cm",
              ].map((size) => (
                <button
                  key={size}
                  className="border p-2 rounded hover:bg-gray-100"
                >
                  {size}
                </button>
              ))}
            </div>
          </div>

          {/* Stock Info */}
          <div className="text-sm text-green-600">
            残り11点 ご注文はお早めに
          </div>
          <div className="text-sm text-gray-500">今週109人が購入しました</div>

          {/* Buttons */}
          <div className="flex flex-col gap-2">
            <button className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700">
              カートに入れる
            </button>
          </div>
        </div>
      </div>

      {/* Description Section */}
      <div className="mt-10">
        <h2 className="text-xl font-bold mb-2">商品の説明</h2>
        <p className="text-gray-700 text-sm leading-relaxed">
          This product is designed for comfort and durability. It’s lightweight
          and breathable, ideal for everyday use, running, and sports. The
          cushioned sole evenly distributes weight, reduces pressure on the
          spine, and supports long-term wear.
        </p>
      </div>
    </div>
  );
}
