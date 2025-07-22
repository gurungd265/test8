import { Link } from "react-router-dom";
import React from 'react';
import cartApi from '../api/cart';

export default function Product({ product }) {
  //カートに商品を追加する臨時ハンドラー(cartApi連動は後で)
    const handleAddToCart = async(e) => {
      e.preventDefault(); // Linkタグへの移動防止
      if (product.stockQuantity <= 0) {
        alert("この商品は現在、在庫がありません。");
        return;
      }
      try {
          // 実際のcartApi.addToCart呼び出し（基本数量1個）
          await cartApi.addToCart(product.id, 1);
          alert(`${product.name} をカートに追加しました！`);
          console.log(`カートに ${product.name} を追加しました。`);
      } catch (err) {
          console.error('カートに追加できませんでした。', err);
          if (err.response && err.response.status === 401) {
              alert('カートに追加するにはログインが必要です。');
          } else if (err.response && err.response.data && err.response.data.message) {
              alert(`カートに追加中にエラーが発生しました: ${err.response.data.message}`); //backend
          } else {
              alert('カートに追加中に不明なエラーが発生しました。');
          }
      }
    };

    // 表示される価格(割引価格があれば割引価格、なければ原価)
    const displayPrice = product.discountPrice !== null && product.discountPrice !== undefined
                         ? product.discountPrice
                         : product.price;

    return (
      <>
        {/* Product */}
        <div className="bg-white rounded shadow overflow-hidden">
          {/* Product Image */}
          <Link to={`/product/${product.id}`}>
            <img
              src={product.imageUrl} {/* 変更: product.image -> product.imageUrl */}
              alt={product.name}
              className="w-full h-56 object-cover"
            />
          </Link>
          <div className="p-4">
            {/* Product Name */}
            <h2 className="font-semibold text-lg truncate">
              <Link to={`/product/${product.id}`}>
                {product.name}
              </Link>
            </h2>
            {/* Product Description */}
            <p className="text-sm text-gray-500 truncate">
              {product.description}
            </p>
            <div className="mt-2 flex items-center justify-between">
              {/* Product Price */}
              <span className="font-bold text-lg">
                {product.discountPrice !== null && product.discountPrice !== undefined ? (
                  <>
                    {/* 割引前の価格 */}
                    <span className="line-through text-gray-500 text-base mr-2">{product.price.toLocaleString()}円</span>
                    {/* 割引価格 */}
                    <span className="text-purple-600">{displayPrice.toLocaleString()}円</span>
                  </>
                ) : (
                  <span>{displayPrice.toLocaleString()}円</span> {/* 元値 */}
                )}
              </span>
              {/* Button of Adding To Cart */}
              <button
                onClick={handleAddToCart}
                className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={product.stockQuantity <= 0}
              >
                {/*Add to cart -> カートに入れる,在庫切れ */}
                {product.stockQuantity > 0 ? "カートに入れる" : "在庫切れ"}
              </button>
            </div>
          </div>
        </div>
      </>
    );
}
