<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class HangHangKhong extends Model
{
    protected $table = 'hang_hang_khong';
    protected $fillable = [
        'ten_hang',
        'ma_hang',
        'logo_url',
        'trang_thai'
    ];

    // Quan hệ
    public function nguoi_dung()
    {
        return $this->hasMany(NguoiDung::class, 'ma_hang_hang_khong');
    }

    public function may_bay()
    {
        return $this->hasMany(MayBay::class, 'ma_hang_hang_khong');
    }

    public function chuyen_bay()
    {
        return $this->hasMany(ChuyenBay::class, 'ma_hang_hang_khong');
    }
}
