<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class CauHinhHeThong extends Model
{
    protected $table = 'cau_hinh_he_thong';
    protected $fillable = [
        'ten_cau_hinh',
        'gia_tri'
    ];

    // Helper: get value by key
    public static function get($key, $default = null)
    {
        $config = self::where('ten_cau_hinh', $key)->first();
        return $config ? $config->gia_tri : $default;
    }
}
