import { Link } from "react-router-dom";
import { useState } from "react";
import products from "../data/products.json";

export default function WishesPage() {
  const [wishes, setWishes] = useState(products);

  const handleRemove = (id) => {
    setWishes(wishes.filter((item) => item.id !== id));
  };

  return (
    <div className="max-w-6xl mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">マイリスト</h1>

      {wishes.length === 0 ? (
        <div className="text-gray-600 text-center py-10">
          ウィッシュリストにアイテムがありません。
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
          {wishes.map((item) => (
            <div
              key={item.id}
              className="bg-white rounded overflow-hidden p-4 flex flex-col justify-between hover:shadow-lg duration-500"
            >
              <Link to={`/product/${item.id}`}>
                <img
                  src={item.image}
                  alt={item.name}
                  className="w-full h-48 object-cover rounded-lg"
                />
                <h2 className="font-semibold text-lg mt-2 hover:underline truncate">
                  {item.name}
                </h2>
              </Link>
              {/* Remove Button */}
              <div className="flex justify-between items-center mt-4">
                <span className="font-bold ">
                  {item.price.toLocaleString()}￥
                </span>
                <button
                  onClick={() => handleRemove(item.id)}
                  className="text-red-500 text-sm hover:underline"
                >
                  削除
                </button>
              </div>

              {/* Add to Cart Button */}
              <button
                onClick={() => alert("カートに追加しました")}
                className="w-full bg-purple-600 text-white px-4 py-2 rounded mt-2 hover:bg-purple-700"
                disabled={item.stockQuantity <= 0}
              >
                カートに入れる
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
