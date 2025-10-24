<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('hang_hang_khong', function (Blueprint $table) {
            $table->id();
            $table->string('ten_hang');
            $table->string('ma_hang', 10)->unique(); // VN, VJ...
            $table->string('logo_url')->nullable();
            $table->enum('trang_thai', ['hoat_dong', 'bi_dinh_chi'])->default('hoat_dong');
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('hang_hang_khong');
    }
};
