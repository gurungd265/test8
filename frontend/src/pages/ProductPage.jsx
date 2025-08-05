import React, { useState, useEffect, useContext, useMemo } from "react";
import { useParams, Link } from "react-router-dom";
import productsApi from '../api/products';
import cartApi from '../api/cart';
import wishlistApi from '../api/wishlist';
import { Heart } from "lucide-react";
import { CartContext } from '../contexts/CartContext';
import { useAuth } from "../contexts/AuthContext.jsx";
import ImageSlider from "../components/ImageSlider.jsx";

export default function ProductPage() {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [mainImage, setMainImage] = useState('');
    const productRating = product?.rating ?? 0;
    const productReviewCount = product?.reviewCount ?? 0;
    const { fetchCartCount } = useContext(CartContext);
    const [wished, setWished] = useState(false); // お気に入りに追加するための状態
    const {isLoggedIn,loading: authLoading} = useAuth();


    // 1. 상태 추가: 옵션 선택 저장 (예: { Color: 'ブラック', Size: 'Mサイズ' })
    const [selectedOptions, setSelectedOptions] = useState({});

    // 2. 옵션 선택 핸들러
    const handleOptionChange = (key, value) => {
        setSelectedOptions(prev => {
            const updated = { ...prev, [key]: value };
            console.log("선택된 옵션:", updated); // ✅ 여기에
            return updated;
        });
    };
    // 3. 옵션 데이터를 key별로 그룹핑하는 헬퍼 함수
    const groupedOptionValues = useMemo(() => {
        if (!product?.characteristics) return {};

        return product.characteristics.reduce((acc, item) => {
            const key = item.characteristicName || 'Unknown';
            if (!acc[key]) acc[key] = [];
            if (!acc[key].includes(item.characteristicValue)) {
                acc[key].push(item.characteristicValue);
            }
            return acc;
        }, {});
    }, [product]);

    useEffect(() => {
        const checkWishStatus = async () => {
            if (authLoading || !isLoggedIn) {
                setWished(false);
                return;
            }

            try {
                const wishlistItems = await wishlistApi.getWishlistItems();
                const isProductWished = wishlistItems.some(item => item.productId === parseInt(id));
                setWished(isProductWished);
            } catch (err) {
                console.error("お気に入りリストの状態を確認できませんでした。", err);
            }
        };

        checkWishStatus();
    }, [id, isLoggedIn, authLoading])

    const handleWishlist = async (e) => {
        e.preventDefault();

        if (!isLoggedIn) {
            alert("お気に入りリスト操作にはログインが必要です。");
            return;
        }

        const previousWished = wished;
        setWished(!previousWished); // optimistic update

        try {
            if (!previousWished) {
                // 추가 요청
                await wishlistApi.addWishlistItem(parseInt(id));
                console.log(`${product.name}をお気に入りリストに追加しました。`);
            } else {
                // 제거 요청
                await wishlistApi.removeWishlistItemByProductId(parseInt(id));
                console.log(`${product.name}をお気に入りリストから削除しました。`);
            }
        } catch (err) {
            console.error("お気に入りリスト操作に失敗しました。", err);
            setWished(previousWished); // 롤백

            const message = err.response?.data || "不明なエラーが発生しました。";
            alert(`お気に入りリスト操作中にエラーが発生しました: ${message}`);
        }
    };

    useEffect(() => {
        console.log("useEffect 실행됨: 상품 데이터 요청 시작, id =", id);
        const fetchProduct = async () => {
            try {
                console.log("fetchProduct 함수 실행");
                setLoading(true);
                setError(null);

                const productIdNum = parseInt(id);
                if (isNaN(productIdNum)) {
                    setError('無効な商品IDです。');
                    setLoading(false);
                    return;
                }

                console.log("API 호출 시작 - productId:", productIdNum);
                const data = await productsApi.getProductById(productIdNum);
                console.log("API 응답:", data);

                console.log("product.id:", data?.id);
                if (data.characteristics && data.characteristics.length > 0) {
                    console.log("✔ characteristics 데이터:", data.characteristics);
                } else {
                    console.log("⚠ characteristics 없음 또는 비어 있음");
                }

                setProduct(data);

                if (data && data.images && data.images.length > 0 && mainImage === '') {
                    setMainImage(data.images[0].imageUrl);
                }
            } catch (err) {
                console.error(`商品ID ${id}の読み込みに失敗しました:`, err);
                if (err.response && err.response.status === 404) {
                    setError('商品を見つけることができませんでした。');
                } else {
                    setError('商品情報を読み込むことができませんでした。サーバーの状態を確認してください。');
                }
            } finally {
                setLoading(false);
            }
        };
        fetchProduct();
    }, [id]);

    if (loading) {
        return (
            <div className="p-6 text-center text-gray-700">商品情報を読み込み中...</div>
        );
    }

    if (error) {
        return (
            <div className="p-6 text-center text-red-600">{error}</div>
        );
    }

    if (!product) {
        return (
            <div className="p-6 text-center text-gray-500">商品情報がありません。</div>
        );
    }

    // DBテーブルのstock_quantity変更
    const handleQuantityChange = (e) => {
        const value = parseInt(e.target.value);
        // 1個以上、そして在庫数量(product.stock Quantity)以下のみ選択可能
        if (!isNaN(value) && value >= 1 && value <= product.stockQuantity) {
            setQuantity(value);
        } else if (value > product.stockQuantity) {
            setQuantity(product.stockQuantity); // 最大在庫数量に設定
            alert(`最大${product.stockQuantity}個まで注文可能です。`);
        } else if (value < 1) {
            setQuantity(1);
            alert('数量は1以上である必要があります。');
        }
    };

    //カートに追加ハンドラー
    const handleAddToCart = async() => {
        if (product.stockQuantity <= 0) {
            alert("この商品は現在、在庫がありません。");
            return;
        }
        if (quantity <= 0) {
            alert("数量は1以上である必要があります。");
            return;
        }
        try {
            // 実際のショッピングカートAPI呼び出し
            await cartApi.addToCart(product.id, quantity);
            alert(`${product.name} ${quantity}個がカートに追加されました！`);
            fetchCartCount();
            // 成功的に追加されると、ショッピングカートページに移動したり、ショッピングカートアイコンのアップデートなど
            // navigate('/cart'); // 必要に応じてショッピングカートページへ自動移動
        } catch (err) {
            console.error('カートに追加できませんでした。', err);
            if (err.response && err.response.status === 401) {
                alert('カートに追加するにはログインが必要です。');
            } else if (err.response && err.response.data && err.response.data.message) {
                alert(`カートに追加中にエラーが発生しました: ${err.response.data.message}`); // backend error
            } else {
                alert('カートに追加中に不明なエラーが発生しました。');
            }
        }
    };

    //表示される価格計算(割引価格があれば割引価格、なければ原価)
    const displayPrice = product.discountPrice !== null && product.discountPrice !== undefined
        ? product.discountPrice
        : product.price;

    return (
        <div className="max-w-7xl mx-auto p-6">
            {/* Breadcrumb */}
            <div className="text-sm text-gray-500 mb-4">
                <Link to={`/`}>
                    <span className="hover:underline cursor-pointer">ホーム</span> /{" "}
                </Link>
                {/* Category back end data in category_id, name another API or Join*/}
                <span className="hover:underline cursor-pointer">カテゴリー</span> /{" "}
                <span className="text-gray-700 font-semibold">{product.name}</span>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Image Section (2/3) */}
                <div className="lg:col-span-2">
                    <div className="relative">
                        {/* Add To Wishlist Button */}
                        <button
                            onClick={handleWishlist}
                            className="absolute bg-white rounded-full top-2 right-2 z-10 w-9 h-9 flex items-center justify-center hover:scale-110 transition-transform"
                            title="お気に入りに追加"
                        >
                            <Heart
                                size={18}
                                className={`stroke-2 ${wished ? "text-purple-600 fill-purple-600" : "text-gray-400"}`}
                            />
                        </button>
                        {/* Image Slider */}
                        <ImageSlider
                            images={product?.images || []}
                            productName={product?.name || "Product"}
                        />

                        {/* Main Image */}
                        {/* <img
                            src={mainImage || 'https://via.placeholder.com/600/CCCCCC/FFFFFF?text=No+Image'}
                            alt={product.name}
                            className="w-full h-[420px] object-cover rounded-lg shadow"
                        /> */}
                    </div>
                    {/* Mini Thumbnails */}
                    <div className="flex gap-2 mt-2">
                        {product.images && product.images.slice(0, 5).map((img, i) => (
                            <img
                                key={i}
                                src={img.imageUrl}
                                alt={`サムネイル ${i + 1}`} // 'Thumbnail' -> 'サムネイル'
                                className={`w-20 h-20 object-cover rounded border ${mainImage === img.imageUrl ? 'border-purple-600 ring-2 ring-purple-600' : 'border-gray-300'} cursor-pointer`}
                                onClick={() =>  setMainImage(img.imageUrl)}
                            />
                        ))}
                        {(!product.images || product.images.length === 0) && (
                            <img
                                src="https://via.placeholder.com/80/CCCCCC/FFFFFF?text=No+Image"
                                alt="No Thumbnail"
                                className="w-20 h-20 object-cover rounded border"
                            />
                        )}
                    </div>

                    {/* Product Info */}
                    <div className="mt-6">
                        <h1 className="text-2xl font-bold">{product.name}</h1>
                        <p className="text-sm text-gray-600 mt-2">{product.description}</p>
                    </div>
                </div>

                {/* Info Section (1/3) */}
                <div className="border rounded-lg shadow p-6 space-y-4">
                    {/* Rating & Reviews (another Options table need. now hardcoding) */}
                    <div>
                        <div className="text-yellow-500 font-bold text-lg">
                            {/* product.rating '評価なし' */}
                            ⭐ {product.rating ? product.rating.toFixed(1) : '評価なし'}
                        </div>
                        <div className="text-sm text-gray-600">
                            {product.reviewCount ? `${product.reviewCount} レビュー` : 'レビューなし'} • 100件以上注文されました {/* 일본어 변경 */}
                        </div>
                    </div>

                    {/* Price */}
                    <div>
                        {product.discountPrice !== null && product.discountPrice !== undefined && (
                            <div className="text-xl text-gray-500 line-through mb-1">
                                {/* 割引前の価格 */}
                                {product.price.toLocaleString()}円
                            </div>
                        )}
                        <div className="text-4xl font-bold text-purple-700 mb-2">
                            {/* displayPrice */}
                            {displayPrice.toLocaleString()}円
                        </div>
                        <div className="text-2xl text-gray-500">
                            {Math.floor(displayPrice / 24).toLocaleString()}円/月 24回払い
                        </div>
                    </div>

                    {/*/!* Characteristic *!/*/}
                    {Object.entries(groupedOptionValues).map(([optionGroup, values]) => (
                        <div key={optionGroup} className="mb-4">
                            <label className="block font-medium mb-1">{optionGroup}</label>
                            <select
                                className="w-full border rounded p-2"
                                value={selectedOptions[optionGroup] || ''}
                                onChange={e => handleOptionChange(optionGroup, e.target.value)}
                            >
                                <option value="">選択してください</option>
                                {values.map((value, i) => (
                                    <option key={i} value={value}>{value}</option>
                                ))}
                            </select>
                        </div>
                    ))}

                    {/* quantity select */}
                    <div className="mb-6 flex items-center gap-4">
                        <label htmlFor="quantity" className="text-lg font-semibold">数量:</label>
                        <input
                            type="number"
                            id="quantity"
                            min="1"
                            max={product.stockQuantity} // DB stock_quantity
                            value={quantity}
                            onChange={handleQuantityChange}
                            className="w-20 p-2 border rounded-md text-center"
                        />
                    </div>

                    {/* Stock Info (DB Table: stock_quantity) */}
                    {product.stockQuantity > 0 ? ( // DB stock_quantity
                        <div className="text-sm text-green-600">
                            残り{product.stockQuantity}点 ご注文はお早めに
                        </div>
                    ) : (
                        <div className="text-sm text-red-600">在庫切れ</div>
                    )}
                    <div className="text-sm text-gray-500">今週109人が購入しました</div>

                    {/* Buttons */}
                    <div className="flex flex-col gap-2">
                        <button
                            onClick={handleAddToCart} // 장바구니 추가 핸들러 연결
                            className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700"
                            disabled={product.stockQuantity <= 0}
                        >
                            カートに入れる
                        </button>
                    </div>
                </div>
            </div>

            {/* Description Section */}
            <div className="mt-10">
                <h2 className="text-xl font-bold mb-2">商品の説明</h2>
                <p className="text-gray-700 text-sm leading-relaxed">
                    {product.description}
                </p>
            </div>
        </div>
    );
}