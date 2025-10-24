<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('tuyen_bay', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('san_bay_di');
            $table->unsignedBigInteger('san_bay_den');
            $table->boolean('duoc_phe_duyet')->default(false);
            $table->timestamps();

            $table->foreign('san_bay_di')->references('id')->on('san_bay')->onDelete('cascade');
            $table->foreign('san_bay_den')->references('id')->on('san_bay')->onDelete('cascade');
            $table->unique(['san_bay_di', 'san_bay_den']);
        });
    }

    public function down()
    {
        Schema::dropIfExists('tuyen_bay');
    }
};
