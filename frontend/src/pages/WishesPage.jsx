import { Link } from "react-router-dom";
import { useState, useEffect } from "react";
import wishlistApi from "../api/wishlist";
import cartApi from "../api/cart";
import { useAuth } from "../contexts/AuthContext";

export default function WishesPage() {
  const { isLoggedIn, loading: authLoading } = useAuth();
  const [wishes, setWishes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchWishlist = async () => {
      if (authLoading) return;
      if (!isLoggedIn) {
        setError("ウィッシュリストを表示するにはログインしてください。");
        setLoading(false);
        return;
      }

      try {
        const data = await wishlistApi.getWishlistItems();
        setWishes(data);
      } catch (err) {
        console.error("ウィッシュリストデータを読み込めませんでした。", err);
        setError("ウィッシュリストデータを読み込めませんでした。");
      } finally {
        setLoading(false);
      }
    };
    fetchWishlist();
  }, [isLoggedIn, authLoading]);

  const handleRemove = (id) => {
    const removeWishlistItem = async () => {
      try {
        await wishlistApi.removeWishlistItem(id);
        setWishes(wishes.filter((item) => item.id !== id)); // 성공 시 UI에서 제거
      } catch (err) {
        console.error(
          `ウィッシュリストアイテムID ${id}の削除に失敗しました。`,
          err
        );
        alert("ウィッシュリストからアイテムを削除できませんでした。"); // 사용자에게 알림
      }
    };
    removeWishlistItem();
  };

  const handleAddToCart = async (productId, quantity = 1) => {
    try {
      await cartApi.addToCart(productId, quantity);
      alert("カートに追加しました！");
    } catch (error) {
      console.error("カートへの追加に失敗しました。", error);
      alert("カートへの追加に失敗しました。");
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">マイリスト</h1>

      {loading ? (
        <div className="text-center py-10">読み込み中...</div>
      ) : error ? (
        <div className="text-red-600 text-center py-10">{error}</div>
      ) : wishes.length === 0 ? (
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
              <Link to={`/product/${item.productId}`}>
                <img
                  src={item.productImageUrl}
                  alt={item.productName}
                  className="w-full h-48 object-cover rounded-lg"
                />
                <h2 className="font-semibold text-lg mt-2 hover:underline truncate">
                  {item.productName}
                </h2>
              </Link>
              {/* Remove Button */}
              <div className="flex justify-between items-center mt-4">
                {/* <span className="font-bold ">
                  {item.productPrice.toLocaleString()}円
                </span> */}

                <button
                  onClick={() => handleRemove(item.id)}
                  className="text-red-500 text-sm hover:underline"
                >
                  削除
                </button>
              </div>

              {/* Add to Cart Button */}
              <button
                onClick={() => handleAddToCart(item.productId)}
                className="w-full bg-purple-600 text-white px-4 py-2 rounded mt-2 hover:bg-purple-700"
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
