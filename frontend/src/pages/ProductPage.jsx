import { useParams, Link } from 'react-router-dom';
import products from '../data/products.json';

export default function ProductPage() {
  const { id } = useParams();
  const product = products.find((p) => p.id === parseInt(id));

  if (!product) {
    return <div className="p-6 text-center text-red-600">Product not found</div>;
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Breadcrumb */}
      <div className="text-sm text-gray-500 mb-4">
        <Link to={`/`}>
          <span className="hover:underline cursor-pointer">Home</span> /{' '}
        </Link>
        <span className="hover:underline cursor-pointer">All Categories</span> /{' '}
        <span className="text-gray-700 font-semibold">{product.name}</span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Image section (2/3) */}
        <div className="lg:col-span-2">
          {/* Main image */}
          <img
            src={product.image}
            alt={product.name}
            className="w-full h-[420px] object-cover rounded-lg shadow"
          />
          {/* Mini thumbnails */}
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

          {/* Product info */}
          <div className="mt-6">
            <h1 className="text-2xl font-bold">{product.name}</h1>
            <p className="text-sm text-gray-600 mt-2">{product.description}</p>
          </div>
        </div>

        {/* Info section (1/3) */}
        <div className="border rounded-lg shadow p-6 space-y-4">
          {/* Rating & Reviews */}
          <div>
            <div className="text-yellow-500 font-bold text-lg">⭐ 4.7</div>
            <div className="text-sm text-gray-600">44 reviews • 200+ orders</div>
          </div>

          {/* Price */}
          <div>
            <div className="text-2xl font-bold text-green-700">{product.price}</div>
            <div className="text-sm text-gray-500">14 095 so'm × 24 oy</div>
          </div>

          {/* Color & Size */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Color:</label>
            <select className="w-full border rounded p-2">
              <option>Ko‘k</option>
              <option>Qora</option>
              <option>Oq</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Size (RUS):</label>
            <div className="grid grid-cols-3 gap-2">
              {[40, 41, 42, 43, 44, 45].map((size) => (
                <button
                  key={size}
                  className="border p-2 rounded hover:bg-gray-100"
                >
                  {size}
                </button>
              ))}
            </div>
          </div>

          {/* Stock info */}
          <div className="text-sm text-green-600">11 items available</div>
          <div className="text-sm text-gray-500">109 people bought this week</div>

          {/* Buttons */}
          <div className="flex flex-col gap-2">
            <button className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700">
              Add to Cart
            </button>
            <button className="w-full border border-purple-600 text-purple-600 py-2 rounded hover:bg-purple-100">
              Buy in 1 click
            </button>
          </div>
        </div>
      </div>

      {/* Description section */}
      <div className="mt-10">
        <h2 className="text-xl font-bold mb-2">Description</h2>
        <p className="text-gray-700 text-sm leading-relaxed">
          This product is designed for comfort and durability. It’s lightweight and breathable,
          ideal for everyday use, running, and sports. The cushioned sole evenly distributes weight,
          reduces pressure on the spine, and supports long-term wear.
        </p>
      </div>
    </div>
  );
}
