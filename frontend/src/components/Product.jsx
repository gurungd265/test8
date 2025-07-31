import { Link } from "react-router-dom";
import cartApi from "../api/cart";
import { Heart } from "lucide-react";
import { useState } from "react";

export default function Product({ product }) {
  const { id, name, price, description, discountPrice, stockQuantity, images } =
    product;
  const mainImageUrl =
    images && images.length > 0
      ? (images.find((img) => img.isPrimary) || images[0]).imageUrl
      : "https://via.placeholder.com/200/CCCCCC/FFFFFF?text=No+Image";
  //カートに商品を追加する臨時ハンドラー(cartApi連動は後で)
  const handleAddToCart = async (e) => {
    e.preventDefault(); // Linkタグへの移動防止
    if (stockQuantity <= 0) {
      alert("この商品は現在、在庫がありません。");
      return;
    }
    try {
      // 実際のcartApi.addToCart呼び出し（基本数量1個）
      await cartApi.addToCart(id, 1);
      alert(`${name} をカートに追加しました！`);
      console.log(`カートに ${name} を追加しました。`);
    } catch (err) {
      console.error("カートに追加できませんでした。", err);
      if (err.response && err.response.status === 401) {
        alert("カートに追加するにはログインが必要です。");
      } else if (
        err.response &&
        err.response.data &&
        err.response.data.message
      ) {
        alert(
          `カートに追加中にエラーが発生しました: ${err.response.data.message}`
        ); //backend
      } else {
        alert("カートに追加中に不明なエラーが発生しました。");
      }
    }
  };
  const [wished, setWished] = useState(false); // マイリストに追加するための状態
  const handleWishlist = () => {
    e.preventDefault(); // Linkタグへの移動防止
    setWished(!wished); // wishedの状態をトグル
    alert(`${name}を${!wished ? "マイリストに追加しました" : "マイリストから削除しました"}！`);}

  // 表示される価格(割引価格があれば割引価格、なければ原価)
  const displayPrice =
    discountPrice !== null && discountPrice !== undefined
      ? discountPrice
      : price;

  return (
    <>
      {/* Product */}
      <div className="bg-white rounded overflow-hidden flex flex-col justify-between hover:shadow-lg duration-500 group relative">
        {/* Add To Wishes */}
        <button
          onClick={handleWishlist}
          className="absolute bg-white rounded-full top-2 right-2 z-10 w-9 h-9 flex items-center justify-center hover:scale-110 transition-trnsfrorm"
          title="マイリストに追加"
        >
          <Heart 
            size={18}
            className={`stroke-2 ${
              wished ? "text-purple-600 fill-purple-600" : "text-gray-400"
            }`}
          />
        </button>

        {/* Product Image */}
        <Link to={`/product/${id}`}>
          <img
            src={mainImageUrl}
            alt={name}
            className="w-full h-300 object-cover rounded-lg group-hover:rounded-t-lg group-hover:rounded-b-none transition-all duration-300"
          />
        </Link>
        <div className="p-4">
          {/* Product Name */}
          <h2 className="font-semibold text-lg truncate">
            <Link to={`/product/${id}`}>{name}</Link>
          </h2>
          {/* Product Description */}
          <p className="text-sm text-gray-500 truncate">{description}</p>
          <div className="mt-2 flex items-center justify-between">
            {/* Product Price */}
            <div className="font-bold text-lg">
              {discountPrice !== null && discountPrice !== undefined ? (
                <>
                  {/* 割引前の価格 */}
                  <div className="line-through text-gray-500 text-base mr-2">
                    {price.toLocaleString()}円
                  </div>
                  {/* 割引価格 */}
                  <div className="text-purple-600">
                    {displayPrice.toLocaleString()}円
                  </div>
                </>
              ) : (
                <span>{displayPrice.toLocaleString()}円</span>
              )}
            </div>
          </div>
          {/* Button of Adding To Cart */}
          <button
            onClick={handleAddToCart}
            className="w-full bg-purple-600 text-white px-4 py-2 rounded mt-2 hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
            disabled={stockQuantity <= 0}
          >
            {/*Add to cart -> カートに入れる,在庫切れ */}
            {stockQuantity > 0 ? "カートに入れる" : "在庫切れ"}
          </button>
        </div>
      </div>
    </>
  );
}
