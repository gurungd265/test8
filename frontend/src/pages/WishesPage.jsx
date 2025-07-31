import { Link } from "react-router-dom";
import { useState, useEffect } from "react";
import productsApi from "../api/products";
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
        const wishlistItems = await wishlistApi.getWishlistItems();

        // 제품 상세정보 병렬 호출
        const detailedItems = await Promise.all(
            wishlistItems.map(async (item) => {
              try {
                const productDetail = await productsApi.getProductById(item.productId);
                console.log("가격 정보:", productDetail.price, productDetail.discountPrice);
                return {
                  ...item,
                  price: productDetail.price,
                  discountPrice: productDetail.discountPrice,
                };
              } catch (e) {
                return { ...item, price: null, discountPrice: null };
              }
            })
        );

        setWishes(detailedItems);
      } catch (err) {
        console.error("ウィッシュリストデータを読み込めませんでした。", err);
        setError("ウィッシュリストデータを読み込めませんでした。");
      } finally {
        setLoading(false);
      }
    };
    fetchWishlist();
  }, [isLoggedIn, authLoading]);

  const handleRemove = (productIdToRemove) => {
    const removeWishlistItemAction = async () => {
      try {
          await wishlistApi.removeWishlistItemByProductId(productIdToRemove);
          setWishes(wishes.filter((item) => item.productId !== productIdToRemove));
      } catch (err) {
        console.error(
          `ウィッシュリストアイテムID ${productIdToRemove}の削除に失敗しました。`,
          err
        );
        alert("ウィッシュリストからアイテムを削除できませんでした。");
      }
    };
    removeWishlistItemAction();
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
        <h1 className="text-2xl font-bold mb-4">お気に入り</h1>

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
              {wishes.map((item) => {
                const hasDiscount = typeof item.discountPrice === "number";
                const displayPrice = hasDiscount ? item.discountPrice : item.price;

                return (
                    <div
                        key={item.id}
                        className="border rounded p-4 flex flex-col items-start"  // items-center -> items-start로 변경
                    >
                      <Link to={`/product/${item.productId}`} className="w-full">
                        <img
                            src={item.productImageUrl}
                            alt={item.productName}
                            className="w-full h-56 object-cover rounded"
                        />
                        <h2 className="text-lg font-semibold mt-2 text-left">{item.productName}</h2>
                      </Link>

                      {/* 가격 표시 */}
                      <div className="flex justify-between items-center mt-2 w-full text-sm">
                        <div className="text-gray-500 line-through">
                          {item.price.toLocaleString()}円
                        </div>
                        <div className="text-purple-700 font-semibold ml-2">
                          {displayPrice.toLocaleString()}円
                        </div>
                        <button
                            onClick={() => handleRemove(item.productId)}
                            className="text-red-600 hover:text-red-800 font-medium underline ml-auto"
                        >
                          削除
                        </button>
                      </div>

                      <button
                          onClick={() => handleAddToCart(item.productId)}
                          className="w-full bg-purple-600 text-white px-4 py-2 rounded mt-2 hover:bg-purple-700"
                      >
                        カートに入れる
                      </button>
                    </div>
                );
              })}
            </div>
        )}
      </div>
  );
}