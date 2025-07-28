import { Link } from "react-router-dom";
import cartApi from "../api/cart";

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

  // 表示される価格(割引価格があれば割引価格、なければ原価)
  const displayPrice =
    discountPrice !== null && discountPrice !== undefined
      ? discountPrice
      : price;

  return (
    <>
      {/* Product */}
      <div className="bg-white rounded overflow-hidden flex flex-col justify-between hover:shadow-lg duration-500">
        {/* Product Image */}
        <Link to={`/product/${id}`}>
          <img
            src={mainImageUrl}
            alt={name}
            className="w-full h-56 object-cover rounded-lg hover:rounded-t-lg"
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
